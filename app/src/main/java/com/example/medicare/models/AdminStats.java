package com.example.medicare.models;

public class AdminStats {
    private int totalDoctors;
    private int totalPatients;
    private int totalAppointments;
    private int pendingAppointments;
    private int confirmedAppointments;
    private int completedAppointments;
    private int rejectedAppointments;
    private int cancelledAppointments;

    public AdminStats() {}

    public AdminStats(int totalDoctors, int totalPatients, int totalAppointments,
                      int pendingAppointments, int confirmedAppointments, int completedAppointments) {
        this.totalDoctors = totalDoctors;
        this.totalPatients = totalPatients;
        this.totalAppointments = totalAppointments;
        this.pendingAppointments = pendingAppointments;
        this.confirmedAppointments = confirmedAppointments;
        this.completedAppointments = completedAppointments;
    }

    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }

    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public int getConfirmedAppointments() { return confirmedAppointments; }
    public void setConfirmedAppointments(int confirmedAppointments) { this.confirmedAppointments = confirmedAppointments; }

    public int getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(int completedAppointments) { this.completedAppointments = completedAppointments; }

    public int getRejectedAppointments() { return rejectedAppointments; }
    public void setRejectedAppointments(int rejectedAppointments) { this.rejectedAppointments = rejectedAppointments; }

    public int getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(int cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
}
