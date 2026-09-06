package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.Doctor;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;

public class DoctorDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ProgressBar progressBarDoctor;
    private View scrollViewDoctor;

    private TextView tvDoctorAvatar;
    private TextView tvDoctorName;
    private TextView tvDoctorSpecialty;
    private TextView tvDoctorQualification;
    private TextView tvHospitalBadge;
    private TextView tvDoctorExperience;
    private TextView tvDoctorDepartment;
    private TextView tvConsultationFee;
    private TextView tvRoomNumber;
    private View dividerContact, layoutContact;
    private TextView tvDoctorContact;
    private MaterialButton btnBookAppointment;

    private String doctorId;
    private String doctorName;
    private String department;
    private double consultationFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);

        btnBack = findViewById(R.id.btnBack);
        progressBarDoctor = findViewById(R.id.progressBarDoctor);
        scrollViewDoctor = findViewById(R.id.scrollViewDoctor);

        tvDoctorAvatar = findViewById(R.id.tvDoctorAvatar);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorSpecialty = findViewById(R.id.tvDoctorSpecialty);
        tvDoctorQualification = findViewById(R.id.tvDoctorQualification);
        tvHospitalBadge = findViewById(R.id.tvHospitalBadge);
        tvDoctorExperience = findViewById(R.id.tvDoctorExperience);
        tvDoctorDepartment = findViewById(R.id.tvDoctorDepartment);
        tvConsultationFee = findViewById(R.id.tvConsultationFee);
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        dividerContact = findViewById(R.id.dividerContact);
        layoutContact = findViewById(R.id.layoutContact);
        tvDoctorContact = findViewById(R.id.tvDoctorContact);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        doctorId = intent.getStringExtra("doctor_id");
        doctorName = intent.getStringExtra("doctor_name");
        department = intent.getStringExtra("department");
        consultationFee = intent.getDoubleExtra("consultation_fee", 0);

        // Pre-populate if extras exist
        if (doctorName != null && !doctorName.isEmpty()) {
            tvDoctorName.setText(doctorName);
            String initial = doctorName.replace("Dr.", "").trim();
            if (!initial.isEmpty()) {
                tvDoctorAvatar.setText(String.valueOf(initial.charAt(0)).toUpperCase());
            }
        }
        if (department != null && !department.isEmpty()) {
            tvDoctorSpecialty.setText(department);
            tvDoctorDepartment.setText(department);
        }
        if (consultationFee > 0) {
            tvConsultationFee.setText("₹" + (int) consultationFee);
        }

        btnBookAppointment.setOnClickListener(v -> {
            Intent bookIntent = new Intent(DoctorDetailsActivity.this, BookAppointmentActivity.class);
            bookIntent.putExtra("doctor_id", doctorId);
            bookIntent.putExtra("doctor_name", doctorName);
            bookIntent.putExtra("department", department);
            bookIntent.putExtra("consultation_fee", consultationFee);
            startActivity(bookIntent);
        });

        loadDoctorDetails();
    }

    private void loadDoctorDetails() {
        if (doctorId == null || doctorId.isEmpty()) {
            return;
        }

        progressBarDoctor.setVisibility(View.VISIBLE);
        SupabaseClient.getInstance().getDoctorById(doctorId, new SupabaseClient.Callback<Doctor>() {
            @Override
            public void onSuccess(Doctor doc) {
                progressBarDoctor.setVisibility(View.GONE);
                bindDoctorData(doc);
            }

            @Override
            public void onError(String error) {
                progressBarDoctor.setVisibility(View.GONE);
                // Keep the bundled intent data if available
            }
        });
    }

    private void bindDoctorData(Doctor doc) {
        if (doc == null) return;

        doctorId = doc.getId();
        doctorName = doc.getName();
        department = doc.getDepartment();
        consultationFee = doc.getConsultationFee();

        tvDoctorName.setText(doc.getName());
        tvDoctorSpecialty.setText(doc.getSpecialization());
        tvDoctorQualification.setText(doc.getQualification() != null && !doc.getQualification().isEmpty()
                ? doc.getQualification()
                : "Medical Specialist");
        tvHospitalBadge.setText(doc.getHospitalName());
        tvDoctorExperience.setText(doc.getExperience() + " Years Experience");
        tvDoctorDepartment.setText(doc.getDepartment());

        if (doc.getConsultationFee() > 0) {
            tvConsultationFee.setText("₹" + (int) doc.getConsultationFee());
        } else {
            tvConsultationFee.setText("Standard Consultation");
        }

        if (doc.getRoomNumber() != null && !doc.getRoomNumber().isEmpty()) {
            tvRoomNumber.setText("Room " + doc.getRoomNumber());
        } else {
            tvRoomNumber.setText("Outpatient Department");
        }

        String contact = null;
        if (doc.getPhone() != null && !doc.getPhone().isEmpty()) {
            contact = doc.getPhone();
        } else if (doc.getEmail() != null && !doc.getEmail().isEmpty()) {
            contact = doc.getEmail();
        }

        if (contact != null && !contact.isEmpty()) {
            dividerContact.setVisibility(View.VISIBLE);
            layoutContact.setVisibility(View.VISIBLE);
            tvDoctorContact.setText(contact);
        } else {
            dividerContact.setVisibility(View.GONE);
            layoutContact.setVisibility(View.GONE);
        }

        String cleanName = doc.getName().replace("Dr.", "").trim();
        if (!cleanName.isEmpty()) {
            tvDoctorAvatar.setText(String.valueOf(cleanName.charAt(0)).toUpperCase());
        }
    }
}
