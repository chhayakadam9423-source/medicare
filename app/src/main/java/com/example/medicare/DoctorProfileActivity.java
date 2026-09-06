package com.example.medicare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

public class DoctorProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ProgressBar pbProfileLoading;

    private TextView tvProfileDoctorName, tvProfileSpecialization, tvProfileHospital;
    private TextView tvProfileQualification, tvProfileExperience, tvProfileFee;
    private TextView tvProfileEmail, tvProfilePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check: Only doctor role is permitted
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isDoctor()) {
            Toast.makeText(this, "Access restricted to doctors.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_doctor_profile);

        initViews();
        setupListeners();
        loadProfileData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        pbProfileLoading = findViewById(R.id.pbProfileLoading);

        tvProfileDoctorName = findViewById(R.id.tvProfileDoctorName);
        tvProfileSpecialization = findViewById(R.id.tvProfileSpecialization);
        tvProfileHospital = findViewById(R.id.tvProfileHospital);

        tvProfileQualification = findViewById(R.id.tvProfileQualification);
        tvProfileExperience = findViewById(R.id.tvProfileExperience);
        tvProfileFee = findViewById(R.id.tvProfileFee);

        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProfileData() {
        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null) return;

        pbProfileLoading.setVisibility(View.VISIBLE);

        // Populate fallback defaults immediately from user model
        String defaultName = user.getFullName() != null ? user.getFullName() : "Doctor";
        tvProfileDoctorName.setText(defaultName);
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "Not provided");

        SupabaseClient.getInstance().getDoctorProfileByUserId(user.getId(), new SupabaseClient.Callback<Doctor>() {
            @Override
            public void onSuccess(Doctor doctor) {
                pbProfileLoading.setVisibility(View.GONE);
                if (doctor != null) {
                    if (doctor.getName() != null && !doctor.getName().isEmpty()) {
                        tvProfileDoctorName.setText(doctor.getName());
                    }

                    if (doctor.getSpecialization() != null && !doctor.getSpecialization().isEmpty()) {
                        tvProfileSpecialization.setText(doctor.getSpecialization());
                    } else {
                        tvProfileSpecialization.setText("General Medicine");
                    }

                    String hospital = doctor.getHospitalName();
                    String room = doctor.getRoomNumber();
                    String hospText = (hospital != null ? hospital : "Medicare Central Hospital") +
                            (room != null && !room.isEmpty() ? " • " + room : "");
                    tvProfileHospital.setText(hospText);

                    if (doctor.getQualification() != null && !doctor.getQualification().isEmpty()) {
                        tvProfileQualification.setText(doctor.getQualification());
                    } else {
                        tvProfileQualification.setText("MBBS, MD (Specialist)");
                    }

                    if (doctor.getExperience() > 0) {
                        tvProfileExperience.setText(doctor.getExperience() + " Years");
                    } else {
                        tvProfileExperience.setText("10+ Years");
                    }

                    if (doctor.getConsultationFee() > 0) {
                        tvProfileFee.setText("₹" + (int) doctor.getConsultationFee() + " per consultation");
                    } else {
                        tvProfileFee.setText("₹500 per consultation");
                    }

                    if (doctor.getEmail() != null && !doctor.getEmail().isEmpty()) {
                        tvProfileEmail.setText(doctor.getEmail());
                    }
                    if (doctor.getPhone() != null && !doctor.getPhone().isEmpty()) {
                        tvProfilePhone.setText(doctor.getPhone());
                    }
                }
            }

            @Override
            public void onError(String error) {
                pbProfileLoading.setVisibility(View.GONE);
                // Non-blocking fallback is already rendered
            }
        });
    }
}
