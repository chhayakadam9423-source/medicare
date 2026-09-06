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

import com.example.medicare.adapters.DoctorAppointmentAdapter;
import com.example.medicare.models.Appointment;
import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class DoctorAppointmentsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAppointmentCountBadge;
    private ChipGroup chipGroupFilters;
    private Chip chipFilterAll, chipFilterPending, chipFilterConfirmed, chipFilterCompleted;
    private ProgressBar pbAppointmentsLoading;
    private LinearLayout layoutErrorAppointments, layoutEmptyAppointments;
    private TextView tvErrorMessage, tvEmptySubtitle;
    private MaterialButton btnRetryAppointments;
    private RecyclerView rvDoctorAppointments;

    private DoctorAppointmentAdapter adapter;
    private final List<Appointment> allDoctorAppointments = new ArrayList<>();
    private String currentDoctorId;
    private String activeFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check: Doctor role required
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isDoctor()) {
            Toast.makeText(this, "Access restricted to doctors.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_doctor_appointments);

        currentDoctorId = getIntent().getStringExtra("doctor_id");
        if (currentDoctorId == null || currentDoctorId.isEmpty()) {
            currentDoctorId = user.getId();
        }

        String initialFilter = getIntent().getStringExtra("initial_filter");
        if (initialFilter != null && !initialFilter.isEmpty()) {
            activeFilter = initialFilter;
        }

        initViews();
        setupRecyclerView();
        setupFilters();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAppointmentCountBadge = findViewById(R.id.tvAppointmentCountBadge);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterPending = findViewById(R.id.chipFilterPending);
        chipFilterConfirmed = findViewById(R.id.chipFilterConfirmed);
        chipFilterCompleted = findViewById(R.id.chipFilterCompleted);
        pbAppointmentsLoading = findViewById(R.id.pbAppointmentsLoading);
        layoutErrorAppointments = findViewById(R.id.layoutErrorAppointments);
        layoutEmptyAppointments = findViewById(R.id.layoutEmptyAppointments);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        btnRetryAppointments = findViewById(R.id.btnRetryAppointments);
        rvDoctorAppointments = findViewById(R.id.rvDoctorAppointments);

        // Apply initial filter chip check
        if ("Pending".equalsIgnoreCase(activeFilter)) {
            chipFilterPending.setChecked(true);
        } else if ("Confirmed".equalsIgnoreCase(activeFilter)) {
            chipFilterConfirmed.setChecked(true);
        } else if ("Completed".equalsIgnoreCase(activeFilter)) {
            chipFilterCompleted.setChecked(true);
        } else {
            chipFilterAll.setChecked(true);
        }
    }

    private void setupRecyclerView() {
        rvDoctorAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(this, appointment -> {
            Intent intent = new Intent(DoctorAppointmentsActivity.this, DoctorAppointmentDetailsActivity.class);
            intent.putExtra("appointment_id", appointment.getId());
            intent.putExtra("doctor_id", currentDoctorId);
            startActivity(intent);
        });
        rvDoctorAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                activeFilter = "All";
                chipFilterAll.setChecked(true);
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterPending) {
                activeFilter = "Pending";
            } else if (checkedId == R.id.chipFilterConfirmed) {
                activeFilter = "Confirmed";
            } else if (checkedId == R.id.chipFilterCompleted) {
                activeFilter = "Completed";
            } else {
                activeFilter = "All";
            }
            applyCurrentFilter();
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRetryAppointments.setOnClickListener(v -> fetchDoctorAppointments());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh appointments upon returning from details activity
        fetchDoctorAppointments();
    }

    private void fetchDoctorAppointments() {
        showLoading();

        // First confirm doctor record ID if needed
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (currentDoctorId == null || currentDoctorId.equals(user.getId())) {
            SupabaseClient.getInstance().getDoctorProfileByUserId(user.getId(), new SupabaseClient.Callback<Doctor>() {
                @Override
                public void onSuccess(Doctor doctor) {
                    if (doctor != null) {
                        currentDoctorId = doctor.getId();
                    }
                    executeAppointmentQuery();
                }

                @Override
                public void onError(String error) {
                    executeAppointmentQuery();
                }
            });
        } else {
            executeAppointmentQuery();
        }
    }

    private void executeAppointmentQuery() {
        SupabaseClient.getInstance().getDoctorAppointments(currentDoctorId, new SupabaseClient.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> list) {
                allDoctorAppointments.clear();
                if (list != null) {
                    allDoctorAppointments.addAll(list);
                }
                applyCurrentFilter();
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });
    }

    private void applyCurrentFilter() {
        List<Appointment> filtered = new ArrayList<>();
        if ("All".equalsIgnoreCase(activeFilter)) {
            filtered.addAll(allDoctorAppointments);
        } else {
            for (Appointment a : allDoctorAppointments) {
                if (activeFilter.equalsIgnoreCase(a.getStatus())) {
                    filtered.add(a);
                }
            }
        }

        tvAppointmentCountBadge.setText(String.valueOf(filtered.size()));

        if (filtered.isEmpty()) {
            showEmpty();
        } else {
            showList(filtered);
        }
    }

    private void showLoading() {
        pbAppointmentsLoading.setVisibility(View.VISIBLE);
        layoutErrorAppointments.setVisibility(View.GONE);
        layoutEmptyAppointments.setVisibility(View.GONE);
        rvDoctorAppointments.setVisibility(View.GONE);
    }

    private void showList(List<Appointment> list) {
        pbAppointmentsLoading.setVisibility(View.GONE);
        layoutErrorAppointments.setVisibility(View.GONE);
        layoutEmptyAppointments.setVisibility(View.GONE);
        rvDoctorAppointments.setVisibility(View.VISIBLE);
        adapter.setAppointments(list);
    }

    private void showEmpty() {
        pbAppointmentsLoading.setVisibility(View.GONE);
        layoutErrorAppointments.setVisibility(View.GONE);
        layoutEmptyAppointments.setVisibility(View.VISIBLE);
        rvDoctorAppointments.setVisibility(View.GONE);

        if ("Pending".equalsIgnoreCase(activeFilter)) {
            tvEmptySubtitle.setText("You have no pending appointment requests at this time.");
        } else if ("Confirmed".equalsIgnoreCase(activeFilter)) {
            tvEmptySubtitle.setText("You have no confirmed upcoming appointments.");
        } else if ("Completed".equalsIgnoreCase(activeFilter)) {
            tvEmptySubtitle.setText("No consultations have been completed yet.");
        } else {
            tvEmptySubtitle.setText("No appointments scheduled under your doctor profile.");
        }
    }

    private void showError(String message) {
        pbAppointmentsLoading.setVisibility(View.GONE);
        layoutErrorAppointments.setVisibility(View.VISIBLE);
        layoutEmptyAppointments.setVisibility(View.GONE);
        rvDoctorAppointments.setVisibility(View.GONE);
        tvErrorMessage.setText("Failed to load appointments: " + message);
    }
}
