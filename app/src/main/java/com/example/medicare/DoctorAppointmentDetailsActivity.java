package com.example.medicare;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.medicare.models.Appointment;
import com.example.medicare.models.Doctor;
import com.example.medicare.models.Patient;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DoctorAppointmentDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ProgressBar pbDetailsLoading, pbConsultationSubmitting;

    // Overview views
    private TextView tvDetailStatus, tvDetailDateTime, tvDetailReason, tvDetailDepartment;

    // Patient views
    private TextView tvDetailPatientName, tvDetailPatientMrn;
    private TextView tvDetailPatientDemographics, tvDetailPatientPhone, tvDetailPatientDiagnosis;

    // Action cards
    private CardView cardPendingActions;
    private MaterialButton btnAcceptAppointment, btnRejectAppointment;

    private CardView cardConfirmedActions;
    private TextInputLayout tilDiagnosis, tilMedicine, tilDosage, tilInstructions;
    private TextInputEditText etDiagnosis, etMedicine, etDosage, etInstructions;
    private MaterialButton btnSubmitConsultation;

    private CardView cardCompletedSummary;
    private TextView tvReadOnlyDiagnosis, tvReadOnlyMedicine, tvReadOnlyDosage, tvReadOnlyInstructions;

    private CardView cardClosedNotice;
    private TextView tvClosedNoticeTitle, tvClosedNoticeText;

    private String appointmentId;
    private String doctorIdExtra;
    private Appointment currentAppointment;

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

        setContentView(R.layout.activity_doctor_appointment_details);

        appointmentId = getIntent().getStringExtra("appointment_id");
        doctorIdExtra = getIntent().getStringExtra("doctor_id");

        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            Toast.makeText(this, "Appointment not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadAppointmentDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        pbDetailsLoading = findViewById(R.id.pbDetailsLoading);
        pbConsultationSubmitting = findViewById(R.id.pbConsultationSubmitting);

        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDateTime = findViewById(R.id.tvDetailDateTime);
        tvDetailReason = findViewById(R.id.tvDetailReason);
        tvDetailDepartment = findViewById(R.id.tvDetailDepartment);

        tvDetailPatientName = findViewById(R.id.tvDetailPatientName);
        tvDetailPatientMrn = findViewById(R.id.tvDetailPatientMrn);
        tvDetailPatientDemographics = findViewById(R.id.tvDetailPatientDemographics);
        tvDetailPatientPhone = findViewById(R.id.tvDetailPatientPhone);
        tvDetailPatientDiagnosis = findViewById(R.id.tvDetailPatientDiagnosis);

        cardPendingActions = findViewById(R.id.cardPendingActions);
        btnAcceptAppointment = findViewById(R.id.btnAcceptAppointment);
        btnRejectAppointment = findViewById(R.id.btnRejectAppointment);

        cardConfirmedActions = findViewById(R.id.cardConfirmedActions);
        tilDiagnosis = findViewById(R.id.tilDiagnosis);
        tilMedicine = findViewById(R.id.tilMedicine);
        tilDosage = findViewById(R.id.tilDosage);
        tilInstructions = findViewById(R.id.tilInstructions);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedicine = findViewById(R.id.etMedicine);
        etDosage = findViewById(R.id.etDosage);
        etInstructions = findViewById(R.id.etInstructions);
        btnSubmitConsultation = findViewById(R.id.btnSubmitConsultation);

        cardCompletedSummary = findViewById(R.id.cardCompletedSummary);
        tvReadOnlyDiagnosis = findViewById(R.id.tvReadOnlyDiagnosis);
        tvReadOnlyMedicine = findViewById(R.id.tvReadOnlyMedicine);
        tvReadOnlyDosage = findViewById(R.id.tvReadOnlyDosage);
        tvReadOnlyInstructions = findViewById(R.id.tvReadOnlyInstructions);

        cardClosedNotice = findViewById(R.id.cardClosedNotice);
        tvClosedNoticeTitle = findViewById(R.id.tvClosedNoticeTitle);
        tvClosedNoticeText = findViewById(R.id.tvClosedNoticeText);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Pending Actions
        btnAcceptAppointment.setOnClickListener(v -> handleAcceptAppointment());
        btnRejectAppointment.setOnClickListener(v -> showRejectConfirmationDialog());

        // Confirmed Action: Submit Consultation
        btnSubmitConsultation.setOnClickListener(v -> handleCompleteConsultation());
    }

    private void loadAppointmentDetails() {
        pbDetailsLoading.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance().getAppointmentById(appointmentId, new SupabaseClient.Callback<Appointment>() {
            @Override
            public void onSuccess(Appointment appt) {
                pbDetailsLoading.setVisibility(View.GONE);
                currentAppointment = appt;

                // Doctor Access Control / Isolation Verification:
                // Verify that the appointment's doctor_id belongs to the authenticated doctor
                User user = SupabaseClient.getInstance().getCurrentUser();
                boolean matchesDoctor = false;
                if (user != null) {
                    if (user.getId().equals(appt.getDoctorId())) {
                        matchesDoctor = true;
                    } else if (doctorIdExtra != null && doctorIdExtra.equals(appt.getDoctorId())) {
                        matchesDoctor = true;
                    } else if (appt.getDoctorName() != null && user.getFullName() != null
                            && appt.getDoctorName().toLowerCase().contains(user.getFullName().toLowerCase())) {
                        matchesDoctor = true;
                    }
                }

                if (!matchesDoctor) {
                    Toast.makeText(DoctorAppointmentDetailsActivity.this, "Unauthorized: You can only view appointments assigned to you.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                renderAppointment(appt);
                loadPatientInfo(appt);
            }

            @Override
            public void onError(String error) {
                pbDetailsLoading.setVisibility(View.GONE);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Failed to load appointment: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderAppointment(Appointment appt) {
        String status = appt.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }
        tvDetailStatus.setText(status);
        applyStatusBadgeStyle(tvDetailStatus, status);

        String date = appt.getAppointmentDate() != null ? appt.getAppointmentDate() : "Date TBD";
        String time = appt.getAppointmentTime() != null ? appt.getAppointmentTime() : "";
        tvDetailDateTime.setText(date + (time.isEmpty() ? "" : " • " + time));

        String reason = appt.getReason();
        tvDetailReason.setText((reason != null && !reason.trim().isEmpty()) ? reason : "General Medical Consultation");

        String dept = appt.getDepartment();
        tvDetailDepartment.setText("Department: " + ((dept != null && !dept.trim().isEmpty()) ? dept : "General Medicine"));

        // Manage action panels according to status
        cardPendingActions.setVisibility(View.GONE);
        cardConfirmedActions.setVisibility(View.GONE);
        cardCompletedSummary.setVisibility(View.GONE);
        cardClosedNotice.setVisibility(View.GONE);

        if ("Pending".equalsIgnoreCase(status)) {
            cardPendingActions.setVisibility(View.VISIBLE);
        } else if ("Confirmed".equalsIgnoreCase(status)) {
            cardConfirmedActions.setVisibility(View.VISIBLE);
        } else if ("Completed".equalsIgnoreCase(status)) {
            cardCompletedSummary.setVisibility(View.VISIBLE);
            tvReadOnlyDiagnosis.setText((appt.getDiagnosis() != null && !appt.getDiagnosis().isEmpty()) ? appt.getDiagnosis() : "No diagnosis recorded.");
            tvReadOnlyMedicine.setText((appt.getMedicine() != null && !appt.getMedicine().isEmpty()) ? appt.getMedicine() : "None prescribed.");
            tvReadOnlyDosage.setText((appt.getDosage() != null && !appt.getDosage().isEmpty()) ? appt.getDosage() : "N/A");
            tvReadOnlyInstructions.setText((appt.getInstructions() != null && !appt.getInstructions().isEmpty()) ? appt.getInstructions() : "N/A");
        } else if ("Rejected".equalsIgnoreCase(status)) {
            cardClosedNotice.setVisibility(View.VISIBLE);
            tvClosedNoticeTitle.setText("Appointment Rejected");
            tvClosedNoticeText.setText("This appointment request was rejected and cannot be modified.");
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            cardClosedNotice.setVisibility(View.VISIBLE);
            tvClosedNoticeTitle.setText("Appointment Cancelled");
            tvClosedNoticeText.setText("This appointment was cancelled by the patient.");
        }
    }

    private void loadPatientInfo(Appointment appt) {
        String patientName = appt.getPatientName();
        if (patientName == null || patientName.trim().isEmpty()) {
            patientName = "Patient";
        }
        tvDetailPatientName.setText(patientName);

        String mrn = appt.getPatientMrn();
        if (mrn != null && !mrn.trim().isEmpty()) {
            tvDetailPatientMrn.setText("MRN: " + mrn);
        } else {
            tvDetailPatientMrn.setText("MRN: MRN-" + Math.abs(patientName.hashCode() % 90000 + 10000));
        }

        // Fetch detailed patient record for age, gender, blood group, phone
        if (appt.getPatientId() != null) {
            SupabaseClient.getInstance().getPatientProfile(appt.getPatientId(), new SupabaseClient.Callback<Patient>() {
                @Override
                public void onSuccess(Patient patient) {
                    if (patient != null) {
                        if (patient.getMrn() != null && !patient.getMrn().isEmpty()) {
                            tvDetailPatientMrn.setText("MRN: " + patient.getMrn());
                        }
                        String demographics = "Age: " + (patient.getAge() > 0 ? patient.getAge() : "N/A") +
                                " • Gender: " + (patient.getGender() != null ? patient.getGender() : "N/A") +
                                " • Blood: " + (patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A");
                        tvDetailPatientDemographics.setText(demographics);

                        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
                            tvDetailPatientPhone.setText("Contact: " + patient.getPhone());
                        } else {
                            tvDetailPatientPhone.setText("Contact: Not registered");
                        }

                        if (patient.getDiagnosis() != null && !patient.getDiagnosis().isEmpty()) {
                            tvDetailPatientDiagnosis.setText("Primary Medical Note: " + patient.getDiagnosis());
                            tvDetailPatientDiagnosis.setVisibility(View.VISIBLE);
                        } else {
                            tvDetailPatientDiagnosis.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    // Fallback to basic details
                    tvDetailPatientDemographics.setText("Demographics: Registered Medicare Patient");
                    tvDetailPatientPhone.setText("Contact: Confidential Patient Record");
                    tvDetailPatientDiagnosis.setVisibility(View.GONE);
                }
            });
        }
    }

    private void handleAcceptAppointment() {
        setPendingButtonsEnabled(false);
        pbDetailsLoading.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance().updateAppointmentStatus(appointmentId, "Confirmed", new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                pbDetailsLoading.setVisibility(View.GONE);
                setPendingButtonsEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Appointment confirmed successfully.", Toast.LENGTH_SHORT).show();
                loadAppointmentDetails();
            }

            @Override
            public void onError(String error) {
                pbDetailsLoading.setVisibility(View.GONE);
                setPendingButtonsEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Failed to confirm: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showRejectConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reject Appointment")
                .setMessage("Are you sure you want to reject this appointment request? The patient will be notified.")
                .setPositiveButton("Reject Appointment", (dialog, which) -> handleRejectAppointment())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleRejectAppointment() {
        setPendingButtonsEnabled(false);
        pbDetailsLoading.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance().updateAppointmentStatus(appointmentId, "Rejected", new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                pbDetailsLoading.setVisibility(View.GONE);
                setPendingButtonsEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Appointment rejected.", Toast.LENGTH_SHORT).show();
                loadAppointmentDetails();
            }

            @Override
            public void onError(String error) {
                pbDetailsLoading.setVisibility(View.GONE);
                setPendingButtonsEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Failed to reject: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setPendingButtonsEnabled(boolean enabled) {
        btnAcceptAppointment.setEnabled(enabled);
        btnRejectAppointment.setEnabled(enabled);
    }

    private void handleCompleteConsultation() {
        tilDiagnosis.setError(null);

        String diagnosis = etDiagnosis.getText() != null ? etDiagnosis.getText().toString().trim() : "";
        String medicine = etMedicine.getText() != null ? etMedicine.getText().toString().trim() : "";
        String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
        String instructions = etInstructions.getText() != null ? etInstructions.getText().toString().trim() : "";

        if (diagnosis.isEmpty()) {
            tilDiagnosis.setError("Diagnosis is required to complete consultation.");
            etDiagnosis.requestFocus();
            return;
        }

        btnSubmitConsultation.setEnabled(false);
        pbConsultationSubmitting.setVisibility(View.VISIBLE);

        SupabaseClient.getInstance().completeConsultation(appointmentId, diagnosis, medicine, dosage, instructions, new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                pbConsultationSubmitting.setVisibility(View.GONE);
                btnSubmitConsultation.setEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Consultation completed successfully.", Toast.LENGTH_SHORT).show();
                loadAppointmentDetails();
            }

            @Override
            public void onError(String error) {
                pbConsultationSubmitting.setVisibility(View.GONE);
                btnSubmitConsultation.setEnabled(true);
                Toast.makeText(DoctorAppointmentDetailsActivity.this, "Consultation submission failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
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
        drawable.setCornerRadius(24f);
        drawable.setColor(bgColor);
        drawable.setStroke(2, textColor);
        badge.setBackground(drawable);
    }
}
