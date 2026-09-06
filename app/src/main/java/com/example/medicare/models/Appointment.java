package com.example.medicare.models;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    @SerializedName("id")
    private String id;

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("doctor_id")
    private String doctorId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("patient_mrn")
    private String patientMrn;

    @SerializedName("doctor_name")
    private String doctorName;

    @SerializedName("department")
    private String department;

    @SerializedName("appointment_date")
    private String appointmentDate;

    @SerializedName("appointment_time")
    private String appointmentTime;

    @SerializedName("reason")
    private String reason;

    // Allowed status values: Pending, Confirmed, Rejected, Completed, Cancelled
    @SerializedName("status")
    private String status;

    @SerializedName("diagnosis")
    private String diagnosis;

    @SerializedName("medicine")
    private String medicine;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("instructions")
    private String instructions;

    @SerializedName("created_at")
    private String createdAt;

    public Appointment() {}

    public Appointment(String id, String patientId, String doctorId, String patientName,
                       String doctorName, String department, String appointmentDate,
                       String appointmentTime, String status, String reason) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reason = reason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientMrn() { return patientMrn; }
    public void setPatientMrn(String patientMrn) { this.patientMrn = patientMrn; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    // Alias for existing code calling getTimeSlot
    public String getTimeSlot() {
        return appointmentTime != null ? appointmentTime : "";
    }
    public void setTimeSlot(String timeSlot) {
        this.appointmentTime = timeSlot;
    }

    public String getStatus() {
        return status != null ? status : "Pending";
    }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getMedicine() { return medicine; }
    public void setMedicine(String medicine) { this.medicine = medicine; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
