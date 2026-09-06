package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.adapters.DoctorManageAdapter;
import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarManageDoctors;
    private MaterialButton btnToolbarAddDoctor;
    private ExtendedFloatingActionButton fabAddDoctor;
    private ImageButton btnRefreshDoctors;
    private TextView tvDoctorCount;
    private ProgressBar progressManageDoctors;
    private LinearLayout layoutEmptyDoctors;
    private RecyclerView rvManageDoctors;
    private DoctorManageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce strict Role-Based Access Control
        if (!verifyAdminAccess()) {
            return;
        }

        setContentView(R.layout.activity_manage_doctors);
        initViews();
        setupListeners();
        setupRecyclerView();
        loadDoctors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (verifyAdminAccess()) {
            loadDoctors();
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
        toolbarManageDoctors = findViewById(R.id.toolbarManageDoctors);
        btnToolbarAddDoctor = findViewById(R.id.btnToolbarAddDoctor);
        fabAddDoctor = findViewById(R.id.fabAddDoctor);
        btnRefreshDoctors = findViewById(R.id.btnRefreshDoctors);
        tvDoctorCount = findViewById(R.id.tvDoctorCount);
        progressManageDoctors = findViewById(R.id.progressManageDoctors);
        layoutEmptyDoctors = findViewById(R.id.layoutEmptyDoctors);
        rvManageDoctors = findViewById(R.id.rvManageDoctors);
    }

    private void setupListeners() {
        toolbarManageDoctors.setNavigationOnClickListener(v -> finish());
        btnRefreshDoctors.setOnClickListener(v -> loadDoctors());

        View.OnClickListener addListener = v -> {
            Intent intent = new Intent(ManageDoctorsActivity.this, AddEditDoctorActivity.class);
            startActivity(intent);
        };

        btnToolbarAddDoctor.setOnClickListener(addListener);
        fabAddDoctor.setOnClickListener(addListener);
    }

    private void setupRecyclerView() {
        adapter = new DoctorManageAdapter(this, new DoctorManageAdapter.OnDoctorActionListener() {
            @Override
            public void onEdit(Doctor doctor) {
                Intent intent = new Intent(ManageDoctorsActivity.this, AddEditDoctorActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("doctor_id", doctor.getId());
                intent.putExtra("user_id", doctor.getUserId());
                intent.putExtra("name", doctor.getName());
                intent.putExtra("specialization", doctor.getSpecialization());
                intent.putExtra("qualification", doctor.getQualification());
                intent.putExtra("experience", doctor.getExperience());
                intent.putExtra("department", doctor.getDepartment());
                intent.putExtra("hospital_name", doctor.getHospitalName());
                intent.putExtra("room_number", doctor.getRoomNumber());
                intent.putExtra("consultation_fee", doctor.getConsultationFee());
                intent.putExtra("phone", doctor.getPhone());
                intent.putExtra("email", doctor.getEmail());
                startActivity(intent);
            }

            @Override
            public void onDelete(Doctor doctor) {
                confirmDoctorDeletion(doctor);
            }
        });

        rvManageDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvManageDoctors.setAdapter(adapter);
    }

    private void loadDoctors() {
        progressManageDoctors.setVisibility(View.VISIBLE);
        layoutEmptyDoctors.setVisibility(View.GONE);

        SupabaseClient.getInstance().getDoctors(new SupabaseClient.Callback<List<Doctor>>() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                progressManageDoctors.setVisibility(View.GONE);
                if (doctors == null || doctors.isEmpty()) {
                    layoutEmptyDoctors.setVisibility(View.VISIBLE);
                    tvDoctorCount.setText("Registered Doctors (0)");
                    adapter.setDoctors(null);
                } else {
                    layoutEmptyDoctors.setVisibility(View.GONE);
                    tvDoctorCount.setText("Registered Doctors (" + doctors.size() + ")");
                    adapter.setDoctors(doctors);
                }
            }

            @Override
            public void onError(String message) {
                progressManageDoctors.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Error loading doctors: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDoctorDeletion(Doctor doctor) {
        String docName = doctor.getName() != null ? doctor.getName() : "this doctor";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Doctor")
                .setMessage("Are you sure you want to remove " + docName + " from the hospital directory?\n\nNote: If active appointments reference this doctor, deletion will be prevented to protect patient medical history.")
                .setPositiveButton("Remove", (dialog, which) -> executeDoctorDeletion(doctor))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void executeDoctorDeletion(Doctor doctor) {
        progressManageDoctors.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance().deleteDoctor(doctor.getId(), new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressManageDoctors.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Doctor removed successfully.", Toast.LENGTH_SHORT).show();
                loadDoctors();
            }

            @Override
            public void onError(String message) {
                progressManageDoctors.setVisibility(View.GONE);
                new MaterialAlertDialogBuilder(ManageDoctorsActivity.this)
                        .setTitle("Cannot Remove Doctor")
                        .setMessage(message != null ? message : "An error occurred while attempting to remove the doctor record.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }
}
