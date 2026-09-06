package com.example.medicare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.adapters.DoctorAdapter;
import com.example.medicare.models.Doctor;
import com.example.medicare.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

public class DoctorsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvDoctors;
    private ProgressBar progressBar;
    private android.widget.TextView tvEmptyDoctors;
    private DoctorAdapter adapter;
    private final List<Doctor> doctorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors);

        btnBack = findViewById(R.id.btnBack);
        rvDoctors = findViewById(R.id.rvDoctors);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyDoctors = findViewById(R.id.tvEmptyDoctors);

        btnBack.setOnClickListener(v -> finish());

        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAdapter(doctorList);
        rvDoctors.setAdapter(adapter);

        loadDoctors();
    }

    private void loadDoctors() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyDoctors.setVisibility(View.GONE);
        SupabaseClient.getInstance().getDoctors(new SupabaseClient.Callback<List<Doctor>>() {
            @Override
            public void onSuccess(List<Doctor> list) {
                progressBar.setVisibility(View.GONE);
                doctorList.clear();
                if (list != null) {
                    doctorList.addAll(list);
                }
                adapter.notifyDataSetChanged();
                tvEmptyDoctors.setVisibility(doctorList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                tvEmptyDoctors.setVisibility(doctorList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
