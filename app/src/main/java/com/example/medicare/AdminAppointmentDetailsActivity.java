package com.example.medicare;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.medicare.models.Patient;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.appbar.MaterialToolbar;

public class AdminAppointmentDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarAdminApptDetails;
    private TextView tvApptId, tvDetailStatusBadge, tvHeaderDateTime;

    // Patient views
    private TextView tvPatientDetailName, tvPatientDetailMrn, tvPatientDetailAge;
    private TextView tvPatientDetailGender, tvPatientDetailBloodGroup, tvPatientDetailPhone;

    // Doctor views
    private TextView tvDoctorDetailName, tvDoctorDetailSpecialization, tvDoctorDetailHospital;

    // Appointment views
    private TextView tvApptDetailDate, tvApptDetailTime, tvApptDetailReason;

    // Clinical views (Read-only)
    private LinearLayout layoutClinicalData, layoutClinicalPendingNotice;
    private TextView tvDetailDiagnosis, tvDetailMedicine, tvDetailDosage, tvDetailInstructions;
    private TextView tvClinicalStatusNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Strict RBAC Access Check
        if (!verifyAdminAccess()) {
            return;
        }

        setContentView(R.layout.activity_admin_appointment_details);
        initViews();
        setupToolbar();
        populateDataFromIntent();
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
        toolbarAdminApptDetails = findViewById(R.id.toolbarAdminApptDetails);
        tvApptId = findViewById(R.id.tvApptId);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvHeaderDateTime = findViewById(R.id.tvHeaderDateTime);

        tvPatientDetailName = findViewById(R.id.tvPatientDetailName);
        tvPatientDetailMrn = findViewById(R.id.tvPatientDetailMrn);
        tvPatientDetailAge = findViewById(R.id.tvPatientDetailAge);
        tvPatientDetailGender = findViewById(R.id.tvPatientDetailGender);
        tvPatientDetailBloodGroup = findViewById(R.id.tvPatientDetailBloodGroup);
        tvPatientDetailPhone = findViewById(R.id.tvPatientDetailPhone);

        tvDoctorDetailName = findViewById(R.id.tvDoctorDetailName);
        tvDoctorDetailSpecialization = findViewById(R.id.tvDoctorDetailSpecialization);
        tvDoctorDetailHospital = findViewById(R.id.tvDoctorDetailHospital);

        tvApptDetailDate = findViewById(R.id.tvApptDetailDate);
        tvApptDetailTime = findViewById(R.id.tvApptDetailTime);
        tvApptDetailReason = findViewById(R.id.tvApptDetailReason);

        layoutClinicalData = findViewById(R.id.layoutClinicalData);
        layoutClinicalPendingNotice = findViewById(R.id.layoutClinicalPendingNotice);
        tvDetailDiagnosis = findViewById(R.id.tvDetailDiagnosis);
        tvDetailMedicine = findViewById(R.id.tvDetailMedicine);
        tvDetailDosage = findViewById(R.id.tvDetailDosage);
        tvDetailInstructions = findViewById(R.id.tvDetailInstructions);
        tvClinicalStatusNotice = findViewById(R.id.tvClinicalStatusNotice);
    }

    private void setupToolbar() {
        toolbarAdminApptDetails.setNavigationOnClickListener(v -> finish());
    }

    private void populateDataFromIntent() {
        Intent intent = getIntent();
        String appointmentId = intent.getStringExtra("appointment_id");
        String patientId = intent.getStringExtra("patient_id");
        String patientName = intent.getStringExtra("patient_name");
        String doctorName = intent.getStringExtra("doctor_name");
        String department = intent.getStringExtra("department");
        String appointmentDate = intent.getStringExtra("appointment_date");
        String appointmentTime = intent.getStringExtra("appointment_time");
        String status = intent.getStringExtra("status");
        String reason = intent.getStringExtra("reason");
        String diagnosis = intent.getStringExtra("diagnosis");
        String medicine = intent.getStringExtra("medicine");
        String dosage = intent.getStringExtra("dosage");
        String instructions = intent.getStringExtra("instructions");

        // Header info
        tvApptId.setText("ID: " + (appointmentId != null ? appointmentId : "N/A"));
        tvHeaderDateTime.setText((appointmentDate != null ? appointmentDate : "Date TBD") + " • " + (appointmentTime != null ? appointmentTime : "Slot TBD"));

        // Status badge
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }
        tvDetailStatusBadge.setText(status.toUpperCase());
        applyStatusBadgeStyle(tvDetailStatusBadge, status);

        // Doctor info
        tvDoctorDetailName.setText(doctorName != null ? doctorName : "Attending Doctor");
        tvDoctorDetailSpecialization.setText(department != null && !department.isEmpty() ? department : "General Practice");
        tvDoctorDetailHospital.setText("Medicare Hospital");

        // Appointment schedule
        tvApptDetailDate.setText(appointmentDate != null ? appointmentDate : "N/A");
        tvApptDetailTime.setText(appointmentTime != null ? appointmentTime : "N/A");
        tvApptDetailReason.setText(reason != null && !reason.trim().isEmpty() ? reason : "General Medical Consultation");

        // Initial patient fallback info
        tvPatientDetailName.setText(patientName != null && !patientName.isEmpty() ? patientName : "Patient Profile");
        tvPatientDetailMrn.setText("MRN-Pending");
        tvPatientDetailAge.setText("N/A");
        tvPatientDetailGender.setText("N/A");
        tvPatientDetailBloodGroup.setText("N/A");
        tvPatientDetailPhone.setVisibility(View.GONE);

        // Fetch detailed patient profile if ID available
        if (patientId != null && !patientId.isEmpty()) {
            SupabaseClient.getInstance().getPatientById(patientId, new SupabaseClient.Callback<Patient>() {
                @Override
                public void onSuccess(Patient p) {
                    if (p != null) {
                        if (p.getName() != null && !p.getName().isEmpty()) {
                            tvPatientDetailName.setText(p.getName());
                        }
                        if (p.getMrn() != null && !p.getMrn().isEmpty()) {
                            tvPatientDetailMrn.setText(p.getMrn());
                        }
                        if (p.getAge() > 0) {
                            tvPatientDetailAge.setText(p.getAge() + " Yrs");
                        }
                        if (p.getGender() != null && !p.getGender().isEmpty()) {
                            tvPatientDetailGender.setText(p.getGender());
                        }
                        if (p.getBloodGroup() != null && !p.getBloodGroup().isEmpty()) {
                            tvPatientDetailBloodGroup.setText(p.getBloodGroup());
                        }
                        if (p.getPhone() != null && !p.getPhone().isEmpty()) {
                            tvPatientDetailPhone.setVisibility(View.VISIBLE);
                            tvPatientDetailPhone.setText("Contact: " + p.getPhone());
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    // Non-fatal, use basic display data
                }
            });
        }

        // Clinical Information (Strictly Read-Only)
        boolean isCompleted = "Completed".equalsIgnoreCase(status);
        if (isCompleted) {
            layoutClinicalData.setVisibility(View.VISIBLE);
            layoutClinicalPendingNotice.setVisibility(View.GONE);

            tvDetailDiagnosis.setText(diagnosis != null && !diagnosis.trim().isEmpty() ? diagnosis : "No clinical diagnosis recorded.");
            tvDetailMedicine.setText(medicine != null && !medicine.trim().isEmpty() ? medicine : "No medicines prescribed.");
            tvDetailDosage.setText(dosage != null && !dosage.trim().isEmpty() ? dosage : "N/A");
            tvDetailInstructions.setText(instructions != null && !instructions.trim().isEmpty() ? instructions : "No additional instructions provided.");
        } else {
            layoutClinicalData.setVisibility(View.GONE);
            layoutClinicalPendingNotice.setVisibility(View.VISIBLE);

            if ("Rejected".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
                tvClinicalStatusNotice.setText("This appointment was " + status.toLowerCase() + ". No clinical consultation records were generated.");
            } else {
                tvClinicalStatusNotice.setText("Consultation status is currently '" + status + "'. Clinical diagnosis, prescriptions, and physician advice will be available once the attending doctor finalizes the consultation.");
            }
        }
    }

    private void applyStatusBadgeStyle(TextView badge, String status) {
        int textColor;
        int bgColor;

        if ("Pending".equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_pending);
            bgColor = ContextCompat.getColor(this, R.color.status_pending_bg);
        } else if ("Confirmed".equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.primary);
            bgColor = ContextCompat.getColor(this, R.color.primary_surface);
        } else if ("Completed".equalsIgnoreCase(status)) {
            textColor = ContextCompat.getColor(this, R.color.status_confirmed);
            bgColor = ContextCompat.getColor(this, R.color.status_confirmed_bg);
        } else {
            textColor = ContextCompat.getColor(this, R.color.status_cancelled);
            bgColor = ContextCompat.getColor(this, R.color.status_cancelled_bg);
        }

        badge.setTextColor(textColor);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(20f);
        drawable.setColor(bgColor);
        drawable.setStroke(2, textColor);
        badge.setBackground(drawable);
    }
}
