package com.example.medicare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.R;
import com.example.medicare.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final List<Appointment> appointments;

    public AppointmentAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment item = appointments.get(position);
        holder.tvDoctorName.setText(item.getDoctorName());

        String dept = item.getDepartment() != null ? item.getDepartment() : "General";
        String reason = (item.getReason() != null && !item.getReason().isEmpty()) ? item.getReason() : "Clinical Consultation";
        holder.tvDepartment.setText(dept + " • " + reason);

        String time = item.getAppointmentTime() != null ? item.getAppointmentTime() : item.getTimeSlot();
        holder.tvDateTime.setText(item.getAppointmentDate() + " • " + time);
        holder.tvPatientNameLabel.setText("Patient: " + item.getPatientName());

        String status = item.getStatus();
        holder.tvStatusBadge.setText(status.toUpperCase());

        if ("Confirmed".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_confirmed));
        } else if ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_cancelled));
        } else {
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending));
        }
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDepartment, tvDateTime, tvPatientNameLabel, tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvPatientNameLabel = itemView.findViewById(R.id.tvPatientNameLabel);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
