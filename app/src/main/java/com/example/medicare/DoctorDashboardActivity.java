package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.medicare.models.Appointment;
import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DoctorDashboardActivity extends AppCompatActivity {

    private TextView tvDoctorWelcome, tvDoctorSpecialty, tvDoctorEmail;
    private TextView tvStatTotal, tvStatPending, tvStatConfirmed, tvStatCompleted;
    private TextView tvActionPendingBadge;
    private ProgressBar pbDashboardLoading;
    private CardView cardMyAppointments, cardDoctorProfile;
    private CardView cardStatTotal, cardStatPending, cardStatConfirmed, cardStatCompleted;
    private MaterialButton btnLogout, btnDoctorLogout;

    private String resolvedDoctorId = null;
    private Doctor currentDoctorProfile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security / Role-Based Access Control check:
        // Only authenticated users with role == "doctor" are permitted.
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isDoctor()) {
            if (user != null && user.isPatient()) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (user != null && user.isAdmin()) {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_doctor_dashboard);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvDoctorWelcome = findViewById(R.id.tvDoctorWelcome);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail);

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatConfirmed = findViewById(R.id.tvStatConfirmed);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvActionPendingBadge = findViewById(R.id.tvActionPendingBadge);
        pbDashboardLoading = findViewById(R.id.pbDashboardLoading);

        cardMyAppointments = findViewById(R.id.cardMyAppointments);
        cardDoctorProfile = findViewById(R.id.cardDoctorProfile);
        cardStatTotal = findViewById(R.id.cardStatTotal);
        cardStatPending = findViewById(R.id.cardStatPending);
        cardStatConfirmed = findViewById(R.id.cardStatConfirmed);
        cardStatCompleted = findViewById(R.id.cardStatCompleted);

        btnLogout = findViewById(R.id.btnLogout);
        btnDoctorLogout = findViewById(R.id.btnDoctorLogout);

        // Default initial display from user model
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getFullName();
            if (name == null || name.trim().isEmpty()) {
                name = "Doctor";
            }
            tvDoctorWelcome.setText("Welcome, " + name);
            if (user.getEmail() != null) {
                tvDoctorEmail.setText(user.getEmail());
            }
            resolvedDoctorId = user.getId();
        }
    }

    private void setupListeners() {
        View.OnClickListener logoutListener = v -> handleLogout();
        btnLogout.setOnClickListener(logoutListener);
        btnDoctorLogout.setOnClickListener(logoutListener);

        cardMyAppointments.setOnClickListener(v -> openAppointments(null));
        cardDoctorProfile.setOnClickListener(v -> openProfile());

        // Quick click on stat cards opens appointments with that specific filter
        if (cardStatTotal != null) {
            cardStatTotal.setOnClickListener(v -> openAppointments(null));
        }
        if (cardStatPending != null) {
            cardStatPending.setOnClickListener(v -> openAppointments("Pending"));
        }
        if (cardStatConfirmed != null) {
            cardStatConfirmed.setOnClickListener(v -> openAppointments("Confirmed"));
        }
        if (cardStatCompleted != null) {
            cardStatCompleted.setOnClickListener(v -> openAppointments("Completed"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorData();
    }

    private void loadDoctorData() {
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isDoctor()) {
            return;
        }

        if (pbDashboardLoading != null) {
            pbDashboardLoading.setVisibility(View.VISIBLE);
        }

        // Fetch Doctor details
        SupabaseClient.getInstance().getDoctorProfileByUserId(user.getId(), new SupabaseClient.Callback<Doctor>() {
            @Override
            public void onSuccess(Doctor doctor) {
                currentDoctorProfile = doctor;
                if (doctor != null) {
                    resolvedDoctorId = doctor.getId();
                    String doctorName = doctor.getName();
                    if (doctorName == null || doctorName.isEmpty()) {
                        doctorName = user.getFullName();
                    }
                    tvDoctorWelcome.setText("Welcome, " + doctorName);

                    String dept = doctor.getSpecialization();
                    String hospital = doctor.getHospitalName();
                    tvDoctorSpecialty.setText(dept + " • " + hospital);

                    if (doctor.getEmail() != null && !doctor.getEmail().isEmpty()) {
                        tvDoctorEmail.setText(doctor.getEmail());
                    }
                }
                loadDoctorAppointments();
            }

            @Override
            public void onError(String error) {
                // Non-blocking fallback to user ID
                resolvedDoctorId = user.getId();
                loadDoctorAppointments();
            }
        });
    }

    private void loadDoctorAppointments() {
        String docId = (resolvedDoctorId != null) ? resolvedDoctorId : SupabaseClient.getInstance().getCurrentUser().getId();

        SupabaseClient.getInstance().getDoctorAppointments(docId, new SupabaseClient.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> list) {
                if (pbDashboardLoading != null) {
                    pbDashboardLoading.setVisibility(View.GONE);
                }

                int total = list != null ? list.size() : 0;
                int pending = 0;
                int confirmed = 0;
                int completed = 0;

                if (list != null) {
                    for (Appointment a : list) {
                        String status = a.getStatus();
                        if (status != null) {
                            if ("Pending".equalsIgnoreCase(status)) {
                                pending++;
                            } else if ("Confirmed".equalsIgnoreCase(status)) {
                                confirmed++;
                            } else if ("Completed".equalsIgnoreCase(status)) {
                                completed++;
                            }
                        }
                    }
                }

                tvStatTotal.setText(String.valueOf(total));
                tvStatPending.setText(String.valueOf(pending));
                tvStatConfirmed.setText(String.valueOf(confirmed));
                tvStatCompleted.setText(String.valueOf(completed));

                if (pending > 0) {
                    tvActionPendingBadge.setVisibility(View.VISIBLE);
                    tvActionPendingBadge.setText(pending + " Pending");
                } else {
                    tvActionPendingBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                if (pbDashboardLoading != null) {
                    pbDashboardLoading.setVisibility(View.GONE);
                }
                Toast.makeText(DoctorDashboardActivity.this, "Could not load stats: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAppointments(String filterStatus) {
        Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
        if (resolvedDoctorId != null) {
            intent.putExtra("doctor_id", resolvedDoctorId);
        }
        if (filterStatus != null) {
            intent.putExtra("initial_filter", filterStatus);
        }
        startActivity(intent);
    }

    private void openProfile() {
        Intent intent = new Intent(this, DoctorProfileActivity.class);
        if (resolvedDoctorId != null) {
            intent.putExtra("doctor_id", resolvedDoctorId);
        }
        startActivity(intent);
    }

    private void handleLogout() {
        SupabaseClient.getInstance().signOut(this);
        Intent intent = new Intent(DoctorDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
