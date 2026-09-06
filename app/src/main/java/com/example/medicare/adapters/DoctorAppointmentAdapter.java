package com.example.medicare.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.R;
import com.example.medicare.models.Appointment;

import java.util.ArrayList;
import java.util.List;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private final Context context;
    private final List<Appointment> appointmentList = new ArrayList<>();
    private final OnAppointmentClickListener listener;

    public DoctorAppointmentAdapter(Context context, OnAppointmentClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> list) {
        appointmentList.clear();
        if (list != null) {
            appointmentList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment item = appointmentList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPatientName;
        private final TextView tvPatientMrn;
        private final TextView tvAppointmentStatus;
        private final TextView tvAppointmentDateTime;
        private final TextView tvAppointmentReason;
        private final TextView tvConsultationDoneNotice;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientMrn = itemView.findViewById(R.id.tvPatientMrn);
            tvAppointmentStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            tvAppointmentDateTime = itemView.findViewById(R.id.tvAppointmentDateTime);
            tvAppointmentReason = itemView.findViewById(R.id.tvAppointmentReason);
            tvConsultationDoneNotice = itemView.findViewById(R.id.tvConsultationDoneNotice);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointmentList.get(pos));
                }
            });
        }

        public void bind(Appointment item) {
            String name = item.getPatientName();
            if (name == null || name.trim().isEmpty()) {
                name = "Patient (ID: " + (item.getPatientId() != null ? item.getPatientId().substring(0, Math.min(8, item.getPatientId().length())) : "N/A") + ")";
            }
            tvPatientName.setText(name);

            // MRN display
            String mrn = item.getPatientMrn();
            if (mrn != null && !mrn.trim().isEmpty()) {
                tvPatientMrn.setVisibility(View.VISIBLE);
                tvPatientMrn.setText("MRN: " + mrn);
            } else {
                tvPatientMrn.setVisibility(View.GONE);
            }

            // Date and Time
            String date = item.getAppointmentDate() != null ? item.getAppointmentDate() : "Date TBD";
            String time = item.getAppointmentTime() != null ? item.getAppointmentTime() : "";
            tvAppointmentDateTime.setText(date + (time.isEmpty() ? "" : " • " + time));

            // Reason
            String reason = item.getReason();
            if (reason != null && !reason.trim().isEmpty()) {
                tvAppointmentReason.setText(reason);
            } else {
                tvAppointmentReason.setText("General Consultation");
            }

            // Status Badge with contextual coloring
            String status = item.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Pending";
            }
            tvAppointmentStatus.setText(status);
            applyStatusBadgeStyle(tvAppointmentStatus, status);

            // Completed notice
            if ("Completed".equalsIgnoreCase(status)) {
                tvConsultationDoneNotice.setVisibility(View.VISIBLE);
            } else {
                tvConsultationDoneNotice.setVisibility(View.GONE);
            }
        }

        private void applyStatusBadgeStyle(TextView badge, String status) {
            int textColor;
            int bgColor;

            if ("Pending".equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_pending);
                bgColor = ContextCompat.getColor(context, R.color.status_pending_bg);
            } else if ("Confirmed".equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.primary);
                bgColor = ContextCompat.getColor(context, R.color.primary_surface);
            } else if ("Completed".equalsIgnoreCase(status)) {
                textColor = ContextCompat.getColor(context, R.color.status_confirmed);
                bgColor = ContextCompat.getColor(context, R.color.status_confirmed_bg);
            } else {
                // Rejected or Cancelled
                textColor = ContextCompat.getColor(context, R.color.status_cancelled);
                bgColor = ContextCompat.getColor(context, R.color.status_cancelled_bg);
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
}
