package com.example.medicare.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    // Roles: "patient", "doctor", "admin"
    @SerializedName("role")
    private String role;

    @SerializedName("phone")
    private String phone;

    public User() {}

    public User(String id, String email, String fullName, String role, String phone) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role != null ? role : "patient";
        this.phone = phone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getName() { return fullName; }
    public void setName(String name) { this.fullName = name; }

    public String getRole() { return role != null ? role : "patient"; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isPatient() {
        return "patient".equalsIgnoreCase(getRole());
    }

    public boolean isDoctor() {
        return "doctor".equalsIgnoreCase(getRole());
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole());
    }
}
