package com.example.medicare.models;

import com.google.gson.annotations.SerializedName;

public class Patient {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("name")
    private String name;

    @SerializedName("mrn")
    private String mrn; // Medical Record Number

    @SerializedName("age")
    private int age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("blood_group")
    private String bloodGroup;

    @SerializedName("phone")
    private String phone;

    @SerializedName("diagnosis")
    private String diagnosis;

    public Patient() {}

    public Patient(String id, String name, String mrn, int age, String gender,
                   String bloodGroup, String phone, String diagnosis) {
        this.id = id;
        this.name = name;
        this.mrn = mrn;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.diagnosis = diagnosis;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getMedicalHistory() { return diagnosis; }
    public void setMedicalHistory(String medicalHistory) { this.diagnosis = medicalHistory; }
}
