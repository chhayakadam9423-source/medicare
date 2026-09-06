package com.example.medicare.models;

import com.google.gson.annotations.SerializedName;

public class Doctor {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("specialization")
    private String specialization;

    @SerializedName("qualification")
    private String qualification;

    @SerializedName("experience")
    private int experience;

    @SerializedName("hospital_name")
    private String hospitalName;

    @SerializedName("consultation_fee")
    private double consultationFee;

    @SerializedName("department")
    private String department;

    @SerializedName("room_number")
    private String roomNumber;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    public Doctor() {}

    public Doctor(String id, String name, String specialization, String qualification,
                  int experience, String hospitalName, double consultationFee,
                  String department, String roomNumber) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experience = experience;
        this.hospitalName = hospitalName;
        this.consultationFee = consultationFee;
        this.department = department;
        this.roomNumber = roomNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() {
        return specialization != null ? specialization : department;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // Backwards compatibility alias for specialty
    public String getSpecialty() {
        return getSpecialization();
    }
    public void setSpecialty(String specialty) {
        this.specialization = specialty;
    }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getHospitalName() {
        return hospitalName != null ? hospitalName : "Medicare Hospital";
    }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getDepartment() {
        return department != null ? department : specialization;
    }
    public void setDepartment(String department) { this.department = department; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
