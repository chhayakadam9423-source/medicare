package com.example.medicare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicare.models.Appointment;
import com.example.medicare.models.Doctor;
import com.example.medicare.models.User;
import com.example.medicare.network.SupabaseClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private EditText etPatientName, etDate, etReason;
    private AutoCompleteTextView actvDepartment, actvDoctor, actvTime;
    private Button btnSubmitBooking;
    private ImageButton btnBack;
    private ProgressBar progressBarBooking;

    private final String[] departments = {
            "Cardiology", "Dermatology", "Orthopedics", "General Medicine", "Neurology", "Pediatrics"
    };

    private final String[] timeSlots = {
            "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
    };

    private final List<Doctor> availableDoctors = new ArrayList<>();
    private final List<String> doctorNames = new ArrayList<>();
    private String selectedDoctorId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        etPatientName = findViewById(R.id.etPatientName);
        etDate = findViewById(R.id.etDate);
        etReason = findViewById(R.id.etReason);
        actvDepartment = findViewById(R.id.actvDepartment);
        actvDoctor = findViewById(R.id.actvDoctor);
        actvTime = findViewById(R.id.actvTime);
        btnSubmitBooking = findViewById(R.id.btnSubmitBooking);
        btnBack = findViewById(R.id.btnBack);
        progressBarBooking = findViewById(R.id.progressBarBooking);

        btnBack.setOnClickListener(v -> finish());

        setupPrefilledData();
        setupDropdowns();
        setupDatePicker();

        btnSubmitBooking.setOnClickListener(v -> handleBooking());
    }

    private void setupPrefilledData() {
        User currentUser = SupabaseClient.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getFullName() != null) {
            etPatientName.setText(currentUser.getFullName());
        }

        selectedDoctorId = getIntent().getStringExtra("doctor_id");
        String passedDoc = getIntent().getStringExtra("doctor_name");
        String passedDept = getIntent().getStringExtra("department");
        double fee = getIntent().getDoubleExtra("consultation_fee", 0);

        if (passedDoc != null) {
            actvDoctor.setText(passedDoc, false);
        }
        if (passedDept != null) {
            actvDepartment.setText(passedDept, false);
        }
        TextView tvBookSub = findViewById(R.id.tvBookSub);
        if (tvBookSub != null && fee > 0) {
            tvBookSub.setText("Consultation Fee: ₹" + (int) fee + " • Scheduled at Medicare Hospital");
        }
    }

    private void setupDropdowns() {
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
        actvDepartment.setAdapter(deptAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeSlots);
        actvTime.setAdapter(timeAdapter);

        // Fetch Doctors from Supabase
        SupabaseClient.getInstance().getDoctors(new SupabaseClient.Callback<List<Doctor>>() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                availableDoctors.clear();
                doctorNames.clear();
                availableDoctors.addAll(doctors);

                for (Doctor d : doctors) {
                    doctorNames.add(d.getName());
                }
                ArrayAdapter<String> docAdapter = new ArrayAdapter<>(BookAppointmentActivity.this, android.R.layout.simple_dropdown_item_1line, doctorNames);
                actvDoctor.setAdapter(docAdapter);
            }

            @Override
            public void onError(String error) {
                // Keep pre-filled if any
            }
        });

        actvDoctor.setOnItemClickListener((parent, view, position, id) -> {
            if (position < availableDoctors.size()) {
                Doctor selected = availableDoctors.get(position);
                selectedDoctorId = selected.getId();
                if (selected.getDepartment() != null && !selected.getDepartment().isEmpty()) {
                    actvDepartment.setText(selected.getDepartment(), false);
                }
            }
        });
    }

    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();
        View.OnClickListener dateClickListener = v -> {
            DatePickerDialog dialog = new DatePickerDialog(BookAppointmentActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etDate.setText(formatted);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        };
        etDate.setOnClickListener(dateClickListener);
        View tilDate = findViewById(R.id.tilDate);
        if (tilDate != null) {
            tilDate.setOnClickListener(dateClickListener);
        }
    }

    private void handleBooking() {
        String patientName = etPatientName.getText().toString().trim();
        String department = actvDepartment.getText().toString().trim();
        String doctor = actvDoctor.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = actvTime.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (TextUtils.isEmpty(patientName)) {
            etPatientName.setError("Patient name required");
            return;
        }
        if (TextUtils.isEmpty(department)) {
            actvDepartment.setError("Select department");
            return;
        }
        if (TextUtils.isEmpty(doctor)) {
            actvDoctor.setError("Select doctor");
            return;
        }
        if (TextUtils.isEmpty(date)) {
            etDate.setError("Choose date");
            return;
        }
        if (TextUtils.isEmpty(time)) {
            actvTime.setError("Choose time slot");
            return;
        }

        // Match selectedDoctorId if not already set
        if (selectedDoctorId == null || selectedDoctorId.isEmpty()) {
            for (Doctor d : availableDoctors) {
                if (d.getName().equalsIgnoreCase(doctor)) {
                    selectedDoctorId = d.getId();
                    break;
                }
            }
        }
        if (selectedDoctorId == null || selectedDoctorId.isEmpty()) {
            selectedDoctorId = "doc-" + Math.abs(doctor.hashCode() % 1000);
        }

        progressBarBooking.setVisibility(View.VISIBLE);
        btnSubmitBooking.setEnabled(false);

        User currentUser = SupabaseClient.getInstance().getCurrentUser();
        String patientId = currentUser != null ? currentUser.getId() : null;

        Appointment newAppt = new Appointment(
                null,
                patientId,
                selectedDoctorId,
                patientName,
                doctor,
                department,
                date,
                time,
                "Pending", // Allowed status values: Pending, Confirmed, Rejected, Completed, Cancelled
                TextUtils.isEmpty(reason) ? "General Consultation" : reason
        );

        SupabaseClient.getInstance().createAppointment(newAppt, new SupabaseClient.Callback<Appointment>() {
            @Override
            public void onSuccess(Appointment result) {
                progressBarBooking.setVisibility(View.GONE);
                btnSubmitBooking.setEnabled(true);
                Toast.makeText(BookAppointmentActivity.this, "Appointment scheduled successfully with " + doctor, Toast.LENGTH_LONG).show();
                android.content.Intent myApptIntent = new android.content.Intent(BookAppointmentActivity.this, AppointmentsActivity.class);
                startActivity(myApptIntent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBarBooking.setVisibility(View.GONE);
                btnSubmitBooking.setEnabled(true);
                Toast.makeText(BookAppointmentActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
