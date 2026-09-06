package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.adapters.AppointmentAdapter;
import com.example.medicare.models.Appointment;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser, tvUserRoleBadge, tvViewAllAppointments, tvEmptyAppointments;
    private ImageButton btnLogout;
    private CardView cardBookAppointment, cardAppointments, cardDoctors, cardProfile;
    private RecyclerView rvDashboardAppointments;
    private ProgressBar progressBarAppointments;
    private AppointmentAdapter adapter;
    private final List<Appointment> appointmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check: Only patients can access patient dashboard
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isPatient()) {
            if (user != null && user.isDoctor()) {
                Intent intent = new Intent(this, DoctorDashboardActivity.class);
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

        setContentView(R.layout.activity_dashboard);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserRoleBadge = findViewById(R.id.tvUserRoleBadge);
        tvViewAllAppointments = findViewById(R.id.tvViewAllAppointments);
        tvEmptyAppointments = findViewById(R.id.tvEmptyAppointments);
        btnLogout = findViewById(R.id.btnLogout);

        cardBookAppointment = findViewById(R.id.cardBookAppointment);
        cardAppointments = findViewById(R.id.cardAppointments);
        cardDoctors = findViewById(R.id.cardDoctors);
        cardProfile = findViewById(R.id.cardProfile);

        rvDashboardAppointments = findViewById(R.id.rvDashboardAppointments);
        progressBarAppointments = findViewById(R.id.progressBarAppointments);

        setupUserData();
        setupNavigation();
        setupRecyclerView();
        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }

    private void setupUserData() {
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName()
                    : user.getEmail();
            tvWelcomeUser.setText("Welcome, " + displayName);
            tvUserRoleBadge.setText(user.getRole() != null ? user.getRole().toUpperCase() : "PATIENT");
        } else {
            tvWelcomeUser.setText("Welcome, Clinical User");
            tvUserRoleBadge.setText("HEALTHCARE STAFF");
        }
    }

    private void setupNavigation() {
        btnLogout.setOnClickListener(v -> {
            SupabaseClient.getInstance().signOut(DashboardActivity.this);
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardBookAppointment.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, BookAppointmentActivity.class));
        });

        cardAppointments.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AppointmentsActivity.class));
        });

        cardDoctors.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, DoctorsActivity.class));
        });

        cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, PatientProfileActivity.class));
        });

        tvViewAllAppointments.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AppointmentsActivity.class));
        });
    }

    private void setupRecyclerView() {
        rvDashboardAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(appointmentsList);
        rvDashboardAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        progressBarAppointments.setVisibility(View.VISIBLE);
        tvEmptyAppointments.setVisibility(View.GONE);

        SupabaseClient.getInstance().getAppointments(new SupabaseClient.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> list) {
                progressBarAppointments.setVisibility(View.GONE);
                appointmentsList.clear();
                if (list != null && !list.isEmpty()) {
                    // Show only top 4 for dashboard
                    int limit = Math.min(list.size(), 4);
                    for (int i = 0; i < limit; i++) {
                        appointmentsList.add(list.get(i));
                    }
                    tvEmptyAppointments.setVisibility(View.GONE);
                } else {
                    tvEmptyAppointments.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBarAppointments.setVisibility(View.GONE);
                tvEmptyAppointments.setVisibility(View.VISIBLE);
            }
        });
    }
}
