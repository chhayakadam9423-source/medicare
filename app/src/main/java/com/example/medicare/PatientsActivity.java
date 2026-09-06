package com.example.medicare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.adapters.PatientAdapter;
import com.example.medicare.models.Patient;
import com.example.medicare.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

public class PatientsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvPatients;
    private ProgressBar progressBar;
    private PatientAdapter adapter;
    private final List<Patient> patientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        btnBack = findViewById(R.id.btnBack);
        rvPatients = findViewById(R.id.rvPatients);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        rvPatients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter(patientList);
        rvPatients.setAdapter(adapter);

        loadPatients();
    }

    private void loadPatients() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseClient.getInstance().getPatients(new SupabaseClient.Callback<List<Patient>>() {
            @Override
            public void onSuccess(List<Patient> list) {
                progressBar.setVisibility(View.GONE);
                patientList.clear();
                if (list != null) {
                    patientList.addAll(list);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
