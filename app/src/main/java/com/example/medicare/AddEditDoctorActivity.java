package com.example.medicare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;

public class AddEditDoctorActivity extends AppCompatActivity {

    private MaterialToolbar toolbarAddEditDoctor;
    private TextInputLayout tilDocName, tilDocSpecialization, tilDocQualification;
    private TextInputLayout tilDocExperience, tilDocDepartment, tilDocHospital;
    private TextInputLayout tilDocRoom, tilDocFee, tilDocPhone, tilDocEmail, tilDocUserId;

    private TextInputEditText etDocName, etDocSpecialization, etDocQualification;
    private TextInputEditText etDocExperience, etDocDepartment, etDocHospital;
    private TextInputEditText etDocRoom, etDocFee, etDocPhone, etDocEmail, etDocUserId;

    private ProgressBar progressSaveDoctor;
    private MaterialButton btnSaveDoctor;

    private boolean isEditMode = false;
    private String doctorId = null;
    private String existingUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Strict RBAC access enforcement
        if (!verifyAdminAccess()) {
            return;
        }

        setContentView(R.layout.activity_add_edit_doctor);
        initViews();
        extractIntentData();
        setupListeners();
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
        toolbarAddEditDoctor = findViewById(R.id.toolbarAddEditDoctor);

        tilDocName = findViewById(R.id.tilDocName);
        tilDocSpecialization = findViewById(R.id.tilDocSpecialization);
        tilDocQualification = findViewById(R.id.tilDocQualification);
        tilDocExperience = findViewById(R.id.tilDocExperience);
        tilDocDepartment = findViewById(R.id.tilDocDepartment);
        tilDocHospital = findViewById(R.id.tilDocHospital);
        tilDocRoom = findViewById(R.id.tilDocRoom);
        tilDocFee = findViewById(R.id.tilDocFee);
        tilDocPhone = findViewById(R.id.tilDocPhone);
        tilDocEmail = findViewById(R.id.tilDocEmail);
        tilDocUserId = findViewById(R.id.tilDocUserId);

        etDocName = findViewById(R.id.etDocName);
        etDocSpecialization = findViewById(R.id.etDocSpecialization);
        etDocQualification = findViewById(R.id.etDocQualification);
        etDocExperience = findViewById(R.id.etDocExperience);
        etDocDepartment = findViewById(R.id.etDocDepartment);
        etDocHospital = findViewById(R.id.etDocHospital);
        etDocRoom = findViewById(R.id.etDocRoom);
        etDocFee = findViewById(R.id.etDocFee);
        etDocPhone = findViewById(R.id.etDocPhone);
        etDocEmail = findViewById(R.id.etDocEmail);
        etDocUserId = findViewById(R.id.etDocUserId);

        progressSaveDoctor = findViewById(R.id.progressSaveDoctor);
        btnSaveDoctor = findViewById(R.id.btnSaveDoctor);
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("is_edit_mode", false);

        if (isEditMode) {
            toolbarAddEditDoctor.setTitle("Edit Doctor Details");
            btnSaveDoctor.setText("Update Doctor Profile");

            doctorId = intent.getStringExtra("doctor_id");
            existingUserId = intent.getStringExtra("user_id");

            etDocName.setText(intent.getStringExtra("name"));
            etDocSpecialization.setText(intent.getStringExtra("specialization"));
            etDocQualification.setText(intent.getStringExtra("qualification"));

            int exp = intent.getIntExtra("experience", 0);
            etDocExperience.setText(String.valueOf(exp));

            etDocDepartment.setText(intent.getStringExtra("department"));
            etDocHospital.setText(intent.getStringExtra("hospital_name"));
            etDocRoom.setText(intent.getStringExtra("room_number"));

            double fee = intent.getDoubleExtra("consultation_fee", 0.0);
            if (fee > 0) {
                etDocFee.setText(String.valueOf((int) fee));
            }

            etDocPhone.setText(intent.getStringExtra("phone"));
            etDocEmail.setText(intent.getStringExtra("email"));
            etDocUserId.setText(existingUserId);
        } else {
            toolbarAddEditDoctor.setTitle("Add New Doctor");
            btnSaveDoctor.setText("Register Doctor Profile");
            etDocHospital.setText("Medicare Hospital"); // Helpful default
        }
    }

    private void setupListeners() {
        toolbarAddEditDoctor.setNavigationOnClickListener(v -> finish());
        btnSaveDoctor.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // Clear previous error messages
        tilDocName.setError(null);
        tilDocSpecialization.setError(null);
        tilDocQualification.setError(null);
        tilDocExperience.setError(null);
        tilDocHospital.setError(null);
        tilDocFee.setError(null);

        String name = etDocName.getText() != null ? etDocName.getText().toString().trim() : "";
        String specialization = etDocSpecialization.getText() != null ? etDocSpecialization.getText().toString().trim() : "";
        String qualification = etDocQualification.getText() != null ? etDocQualification.getText().toString().trim() : "";
        String experienceStr = etDocExperience.getText() != null ? etDocExperience.getText().toString().trim() : "";
        String department = etDocDepartment.getText() != null ? etDocDepartment.getText().toString().trim() : "";
        String hospital = etDocHospital.getText() != null ? etDocHospital.getText().toString().trim() : "";
        String room = etDocRoom.getText() != null ? etDocRoom.getText().toString().trim() : "";
        String feeStr = etDocFee.getText() != null ? etDocFee.getText().toString().trim() : "";
        String phone = etDocPhone.getText() != null ? etDocPhone.getText().toString().trim() : "";
        String email = etDocEmail.getText() != null ? etDocEmail.getText().toString().trim() : "";
        String customUserId = etDocUserId.getText() != null ? etDocUserId.getText().toString().trim() : "";

        boolean hasError = false;

        // Validation rule: Name required
        if (TextUtils.isEmpty(name)) {
            tilDocName.setError("Doctor full name is required");
            hasError = true;
        }

        // Validation rule: Specialization required
        if (TextUtils.isEmpty(specialization)) {
            tilDocSpecialization.setError("Specialization is required");
            hasError = true;
        }

        // Validation rule: Qualification required
        if (TextUtils.isEmpty(qualification)) {
            tilDocQualification.setError("Qualification is required (e.g. MBBS, MD)");
            hasError = true;
        }

        // Validation rule: Experience must be numeric
        int experience = 0;
        if (TextUtils.isEmpty(experienceStr)) {
            tilDocExperience.setError("Experience in years is required");
            hasError = true;
        } else {
            try {
                experience = Integer.parseInt(experienceStr);
                if (experience < 0) {
                    tilDocExperience.setError("Experience cannot be negative");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilDocExperience.setError("Experience must be a valid whole number");
                hasError = true;
            }
        }

        // Validation rule: Hospital name required
        if (TextUtils.isEmpty(hospital)) {
            tilDocHospital.setError("Hospital name is required");
            hasError = true;
        }

        // Validation rule: Consultation fee must be numeric
        double fee = 0.0;
        if (TextUtils.isEmpty(feeStr)) {
            tilDocFee.setError("Consultation fee is required");
            hasError = true;
        } else {
            try {
                fee = Double.parseDouble(feeStr);
                if (fee < 0) {
                    tilDocFee.setError("Fee cannot be negative");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilDocFee.setError("Consultation fee must be a valid number");
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        // Default department to specialization if left empty
        if (TextUtils.isEmpty(department)) {
            department = specialization;
        }

        // Build or update Doctor object
        Doctor doctor = new Doctor();
        if (isEditMode) {
            doctor.setId(doctorId);
            doctor.setUserId(!TextUtils.isEmpty(customUserId) ? customUserId : existingUserId);
        } else {
            // New Doctor ID
            String newId = !TextUtils.isEmpty(customUserId) ? customUserId : UUID.randomUUID().toString();
            doctor.setId(newId);
            doctor.setUserId(!TextUtils.isEmpty(customUserId) ? customUserId : newId);
        }

        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setExperience(experience);
        doctor.setDepartment(department);
        doctor.setHospitalName(hospital);
        doctor.setRoomNumber(room);
        doctor.setConsultationFee(fee);
        doctor.setPhone(phone);
        doctor.setEmail(email);

        saveDoctorRecord(doctor);
    }

    private void saveDoctorRecord(Doctor doctor) {
        setLoadingState(true);

        if (isEditMode) {
            SupabaseClient.getInstance().updateDoctor(doctor, new SupabaseClient.Callback<Doctor>() {
                @Override
                public void onSuccess(Doctor result) {
                    setLoadingState(false);
                    Toast.makeText(AddEditDoctorActivity.this, "Doctor updated successfully.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    setLoadingState(false);
                    Toast.makeText(AddEditDoctorActivity.this, "Error updating doctor: " + message, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            SupabaseClient.getInstance().addDoctor(doctor, new SupabaseClient.Callback<Doctor>() {
                @Override
                public void onSuccess(Doctor result) {
                    setLoadingState(false);
                    Toast.makeText(AddEditDoctorActivity.this, "Doctor registered successfully.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    setLoadingState(false);
                    Toast.makeText(AddEditDoctorActivity.this, "Error adding doctor: " + message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setLoadingState(boolean isLoading) {
        progressSaveDoctor.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveDoctor.setEnabled(!isLoading);
        etDocName.setEnabled(!isLoading);
        etDocSpecialization.setEnabled(!isLoading);
        etDocQualification.setEnabled(!isLoading);
        etDocExperience.setEnabled(!isLoading);
        etDocDepartment.setEnabled(!isLoading);
        etDocHospital.setEnabled(!isLoading);
        etDocRoom.setEnabled(!isLoading);
        etDocFee.setEnabled(!isLoading);
        etDocPhone.setEnabled(!isLoading);
        etDocEmail.setEnabled(!isLoading);
    }
}
