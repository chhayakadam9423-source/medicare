package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.Patient;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;

public class PatientProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ProgressBar progressBarProfile;
    private View scrollViewProfile;

    private TextView tvAvatarCircle;
    private TextView tvPatientName;
    private TextView tvPatientEmail;
    private TextView tvPatientMrn;
    private TextView tvRoleBadge;
    private TextView tvPatientAge;
    private TextView tvPatientGender;
    private TextView tvPatientBloodGroup;
    private TextView tvPatientPhone;
    private TextView tvPatientNotes;
    private MaterialButton btnMyAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = SupabaseClient.getInstance().getCurrentUser();
        if (user == null || !user.isPatient()) {
            Toast.makeText(this, "Unauthorized: Patient access only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_patient_profile);

        btnBack = findViewById(R.id.btnBack);
        progressBarProfile = findViewById(R.id.progressBarProfile);
        scrollViewProfile = findViewById(R.id.scrollViewProfile);

        tvAvatarCircle = findViewById(R.id.tvAvatarCircle);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientMrn = findViewById(R.id.tvPatientMrn);
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientBloodGroup = findViewById(R.id.tvPatientBloodGroup);
        tvPatientPhone = findViewById(R.id.tvPatientPhone);
        tvPatientNotes = findViewById(R.id.tvPatientNotes);
        btnMyAppointments = findViewById(R.id.btnMyAppointments);

        btnBack.setOnClickListener(v -> finish());
        btnMyAppointments.setOnClickListener(v -> {
            startActivity(new Intent(PatientProfileActivity.this, AppointmentsActivity.class));
        });

        loadPatientProfile(user);
    }

    private void loadPatientProfile(User user) {
        progressBarProfile.setVisibility(View.VISIBLE);
        scrollViewProfile.setVisibility(View.GONE);

        SupabaseClient.getInstance().getPatientProfile(user.getId(), new SupabaseClient.Callback<Patient>() {
            @Override
            public void onSuccess(Patient patient) {
                progressBarProfile.setVisibility(View.GONE);
                scrollViewProfile.setVisibility(View.VISIBLE);

                String name = (patient != null && patient.getName() != null && !patient.getName().isEmpty())
                        ? patient.getName()
                        : user.getFullName();

                String email = (patient != null && patient.getEmail() != null && !patient.getEmail().isEmpty())
                        ? patient.getEmail()
                        : user.getEmail();

                String phone = (patient != null && patient.getPhone() != null && !patient.getPhone().isEmpty())
                        ? patient.getPhone()
                        : user.getPhone();

                String mrn = (patient != null && patient.getMrn() != null && !patient.getMrn().isEmpty())
                        ? patient.getMrn()
                        : "MRN-" + Math.abs(user.getId().hashCode() % 90000 + 10000);

                int age = (patient != null) ? patient.getAge() : 0;
                String gender = (patient != null && patient.getGender() != null) ? patient.getGender() : "Not Specified";
                String bloodGroup = (patient != null && patient.getBloodGroup() != null) ? patient.getBloodGroup() : "Not Specified";
                String notes = (patient != null && patient.getMedicalHistory() != null) ? patient.getMedicalHistory() : "Regular checkup";

                tvPatientName.setText(name);
                tvPatientEmail.setText(email);
                tvPatientPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not Provided");
                tvPatientMrn.setText(mrn);
                tvRoleBadge.setText("PATIENT");

                if (age > 0) {
                    tvPatientAge.setText(age + " yrs");
                } else {
                    tvPatientAge.setText("Not Provided");
                }

                tvPatientGender.setText(gender);
                tvPatientBloodGroup.setText(bloodGroup);
                tvPatientNotes.setText(notes != null && !notes.isEmpty() ? notes : "No conditions noted");

                if (name != null && !name.trim().isEmpty()) {
                    tvAvatarCircle.setText(String.valueOf(name.trim().charAt(0)).toUpperCase());
                } else {
                    tvAvatarCircle.setText("P");
                }
            }

            @Override
            public void onError(String error) {
                progressBarProfile.setVisibility(View.GONE);
                scrollViewProfile.setVisibility(View.VISIBLE);

                // Fallback to current user data
                tvPatientName.setText(user.getFullName());
                tvPatientEmail.setText(user.getEmail());
                tvPatientPhone.setText(user.getPhone() != null ? user.getPhone() : "Not Provided");
                tvPatientMrn.setText("MRN-" + Math.abs(user.getId().hashCode() % 90000 + 10000));
                tvRoleBadge.setText("PATIENT");
                tvPatientAge.setText("Not Provided");
                tvPatientGender.setText("Not Specified");
                tvPatientBloodGroup.setText("Not Specified");
                tvPatientNotes.setText("General Care");
                if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                    tvAvatarCircle.setText(String.valueOf(user.getFullName().trim().charAt(0)).toUpperCase());
                } else {
                    tvAvatarCircle.setText("P");
                }
            }
        });
    }
}
