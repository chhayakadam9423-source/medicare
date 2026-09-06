package com.example.medicare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.medicare.adapters.AppointmentAdapter;
import com.example.medicare.models.Appointment;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvScreenTitle;
    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupStatus;

    private AppointmentAdapter adapter;
    private final List<Appointment> masterList = new ArrayList<>();
    private final List<Appointment> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        btnBack = findViewById(R.id.btnBack);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);

        User currentUser = SupabaseClient.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isPatient()) {
            if (tvScreenTitle != null) {
                tvScreenTitle.setText("My Appointments");
            }
            if (tvEmpty != null) {
                tvEmpty.setText("No appointments scheduled.\nUse 'Book Appointment' to schedule a consult.");
            }
        }

        btnBack.setOnClickListener(v -> finish());

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(filteredList);
        rvAppointments.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadAppointments);

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> applyFilter(checkedId));

        loadAppointments();
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        SupabaseClient.getInstance().getAppointments(new SupabaseClient.Callback<List<Appointment>>() {
            @Override
            public void onSuccess(List<Appointment> list) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                masterList.clear();
                if (list != null) {
                    masterList.addAll(list);
                }
                applyFilter(chipGroupStatus.getCheckedChipId());
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                applyFilter(chipGroupStatus.getCheckedChipId());
            }
        });
    }

    private void applyFilter(int checkedChipId) {
        filteredList.clear();

        for (Appointment a : masterList) {
            String status = a.getStatus();
            if (checkedChipId == R.id.chipUpcoming) {
                if ("Pending".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status) || "Upcoming".equalsIgnoreCase(status)) {
                    filteredList.add(a);
                }
            } else if (checkedChipId == R.id.chipCompleted) {
                if ("Completed".equalsIgnoreCase(status)) {
                    filteredList.add(a);
                }
            } else {
                // All
                filteredList.add(a);
            }
        }

        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}
