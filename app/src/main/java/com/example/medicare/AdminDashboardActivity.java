package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.medicare.models.AdminStats;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminWelcome, tvAdminEmail;
    private TextView tvTotalDoctors, tvTotalPatients, tvTotalAppointments;
    private TextView tvPendingAppointments, tvConfirmedAppointments, tvCompletedAppointments;
    private ProgressBar progressStats;
    private CardView cardStatsError;
    private TextView tvStatsErrorMessage;
    private MaterialButton btnRetryStats, btnLogout;
    private ImageButton btnRefreshDashboard;
    private CardView cardNavManageDoctors, cardNavAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enforce strict Role-Based Access Control (RBAC)
        if (!verifyAdminAccess()) {
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);
        initViews();
        setupListeners();
        populateAdminProfile();
        loadDashboardStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (verifyAdminAccess()) {
            loadDashboardStatistics();
        }
    }

    /**
     * Strict RBAC check: Only users with role = "admin" can access this dashboard.
     * Checks authenticated Supabase session and cached profile.
     */
    private boolean verifyAdminAccess() {
        SupabaseClient client = SupabaseClient.getInstance();
        User user = client.getCurrentUser();

        // If in-memory user is null, attempt to restore from secured SharedPreferences
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
        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);

        tvTotalDoctors = findViewById(R.id.tvTotalDoctors);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvTotalAppointments = findViewById(R.id.tvTotalAppointments);
        tvPendingAppointments = findViewById(R.id.tvPendingAppointments);
        tvConfirmedAppointments = findViewById(R.id.tvConfirmedAppointments);
        tvCompletedAppointments = findViewById(R.id.tvCompletedAppointments);

        progressStats = findViewById(R.id.progressStats);
        cardStatsError = findViewById(R.id.cardStatsError);
        tvStatsErrorMessage = findViewById(R.id.tvStatsErrorMessage);
        btnRetryStats = findViewById(R.id.btnRetryStats);

        btnRefreshDashboard = findViewById(R.id.btnRefreshDashboard);
        btnLogout = findViewById(R.id.btnLogout);

        cardNavManageDoctors = findViewById(R.id.cardNavManageDoctors);
        cardNavAppointments = findViewById(R.id.cardNavAppointments);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnRefreshDashboard.setOnClickListener(v -> loadDashboardStatistics());
        btnRetryStats.setOnClickListener(v -> loadDashboardStatistics());

        cardNavManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageDoctorsActivity.class);
            startActivity(intent);
        });

        cardNavAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAppointmentsActivity.class);
            startActivity(intent);
        });
    }

    private void populateAdminProfile() {
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getFullName();
            if (name == null || name.trim().isEmpty()) {
                name = "Administrator";
            }
            tvAdminWelcome.setText("Welcome, " + name);
            if (user.getEmail() != null) {
                tvAdminEmail.setText(user.getEmail());
            }
        }
    }

    private void loadDashboardStatistics() {
        progressStats.setVisibility(View.VISIBLE);
        cardStatsError.setVisibility(View.GONE);

        SupabaseClient.getInstance().getAdminDashboardStats(new SupabaseClient.Callback<AdminStats>() {
            @Override
            public void onSuccess(AdminStats stats) {
                progressStats.setVisibility(View.GONE);
                if (stats != null) {
                    tvTotalDoctors.setText(String.valueOf(stats.getTotalDoctors()));
                    tvTotalPatients.setText(String.valueOf(stats.getTotalPatients()));
                    tvTotalAppointments.setText(String.valueOf(stats.getTotalAppointments()));
                    tvPendingAppointments.setText(String.valueOf(stats.getPendingAppointments()));
                    tvConfirmedAppointments.setText(String.valueOf(stats.getConfirmedAppointments()));
                    tvCompletedAppointments.setText(String.valueOf(stats.getCompletedAppointments()));
                }
            }

            @Override
            public void onError(String message) {
                progressStats.setVisibility(View.GONE);
                cardStatsError.setVisibility(View.VISIBLE);
                tvStatsErrorMessage.setText(message != null ? message : "Failed to retrieve statistics.");
            }
        });
    }

    private void handleLogout() {
        SupabaseClient.getInstance().signOut(this);
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
