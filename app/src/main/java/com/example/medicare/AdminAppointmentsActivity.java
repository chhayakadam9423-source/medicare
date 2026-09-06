package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.adapters.AdminAppointmentAdapter;
import com.example.medicare.models.Appointment;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminAppointmentsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarAdminAppointments;
    private ImageButton btnRefreshAppointments;
    private ChipGroup chipGroupStatus;
    private TextView tvAppointmentsCount;
    private ProgressBar progressAdminAppointments;
    private LinearLayout layoutEmptyAppointments;
    private TextView tvEmptySubtitle;
    private RecyclerView rvAdminAppointments;

    private AdminAppointmentAdapter adapter;
    private final List<Appointment> allAppointments = new ArrayList<>();
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Strict RBAC Access Check
        if (!verifyAdminAccess()) {
            return;
        }

        setContentView(R.layout.activity_admin_appointments);
        initViews();
        setupListeners();
        setupRecyclerView();
        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (verifyAdminAccess()) {
            loadAppointments();
        }
    }

    private boolean verifyAdminAccess() {
        SupabaseClient client = SupabaseClient.getInstance();
        User user = client.getCurrentUser();

        if (user == null) {
            client.loadSession(this);
            user = client.getCurrentUser();
        }

        if (user == null || !user.isAdmin()) {
            Toast.makeText(this, "Access Denied: Administrator credentials required.", Toast.LENGTH_LONG).show();
            client.signOut(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    private void initViews() {
        toolbarAdminAppointments = findViewById(R.id.toolbarAdminAppointments);
        btnRefreshAppointments = findViewById(R.id.btnRefreshAppointments);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        tvAppointmentsCount = findViewById(R.id.tvAppointmentsCount);
        progressAdminAppointments = findViewById(R.id.progressAdminAppointments);
        layoutEmptyAppointments = findViewById(R.id.layoutEmptyAppointments);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        rvAdminAppointments = findViewById(R.id.rvAdminAppointments);
    }

    private void setupListeners() {
        toolbarAdminAppointments.setNavigationOnClickListener(v -> finish());
        btnRefreshAppointments.setOnClickListener(v -> loadAppointments());

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                currentFilter = "All";
            } else if (id == R.id.chipPending) {
                currentFilter = "Pending";
            } else if (id == R.id.chipConfirmed) {
                currentFilter = "Confirmed";
            } else if (id == R.id.chipCompleted) {
                currentFilter = "Completed";
            } else if (id == R.id.chipRejected) {
                currentFilter = "Rejected";
            } else if (id == R.id.chipCancelled) {
                currentFilter = "Cancelled";
            }
            applyFilter();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminAppointmentAdapter(this, appointment -> {
            Intent intent = new Intent(AdminAppointmentsActivity.this, AdminAppointmentDetailsActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            intent.putExtra("patient_id", appointment.getPatientId());
            intent.putExtra("doctor_id", appointment.getDoctorId());
            intent.putExtra("patient_name", appointment.getPatientName());
            intent.putExtra("doctor_name", appointment.getDoctorName());
            intent.putExtra("department", appointment.getDepartment());
            intent.putExtra("appointment_date", appointment.getAppointmentDate());
            intent.putExtra("appointment_time", appointment.getAppointmentTime());
            intent.putExtra("status", appointment.getStatus());
            intent.putExtra("reason", appointment.getReason());
            intent.putExtra("diagnosis", appointment.getDiagnosis());
            intent.putExtra("medicine", appointment.getMedicine());
            intent.putExtra("dosage", appointment.getDosage());
            intent.putExtra("instructions", appointment.getInstructions());
            startActivity(intent);
        });

        rvAdminAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAdminAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        progressAdminAppointments.setVisibility(View.VISIBLE);
        layoutEmptyAppointments.setVisibility(View.GONE);

        SupabaseClient.getInstance().getAppointments(new SupabaseClient.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> list) {
                progressAdminAppointments.setVisibility(View.GONE);
                allAppointments.clear();
                if (list != null) {
                    allAppointments.addAll(list);
                }
                applyFilter();
            }

            @Override
            public void onError(String message) {
                progressAdminAppointments.setVisibility(View.GONE);
                Toast.makeText(AdminAppointmentsActivity.this, "Error loading appointments: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFilter() {
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if ("All".equalsIgnoreCase(currentFilter)) {
                filtered.add(a);
            } else if (currentFilter.equalsIgnoreCase(a.getStatus())) {
                filtered.add(a);
            }
        }

        tvAppointmentsCount.setText("Showing " + filtered.size() + " Appointments (" + currentFilter + ")");

        if (filtered.isEmpty()) {
            layoutEmptyAppointments.setVisibility(View.VISIBLE);
            tvEmptySubtitle.setText("No appointments with status '" + currentFilter + "' were found.");
            adapter.setAppointments(null);
        } else {
            layoutEmptyAppointments.setVisibility(View.GONE);
            adapter.setAppointments(filtered);
        }
    }
}
