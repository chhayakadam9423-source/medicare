package com.example.medicare.adapters;

import android.content.Context;
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

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    private final Context context;
    private final List<Appointment> displayedList = new ArrayList<>();
    private final OnAppointmentClickListener listener;

    public AdminAppointmentAdapter(Context context, OnAppointmentClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> list) {
        displayedList.clear();
        if (list != null) {
            displayedList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment item = displayedList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPatientName;
        private final TextView tvPatientMrn;
        private final TextView tvAppointmentStatus;
        private final TextView tvDoctorInfo;
        private final TextView tvAppointmentDateTime;
        private final TextView tvAppointmentReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientMrn = itemView.findViewById(R.id.tvPatientMrn);
            tvAppointmentStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            tvDoctorInfo = itemView.findViewById(R.id.tvDoctorInfo);
            tvAppointmentDateTime = itemView.findViewById(R.id.tvAppointmentDateTime);
            tvAppointmentReason = itemView.findViewById(R.id.tvAppointmentReason);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(displayedList.get(pos));
                }
            });
        }

        public void bind(Appointment item) {
            String patName = item.getPatientName();
            if (patName == null || patName.trim().isEmpty()) {
                patName = "Patient (ID: " + (item.getPatientId() != null ? item.getPatientId().substring(0, Math.min(8, item.getPatientId().length())) : "N/A") + ")";
            }
            tvPatientName.setText(patName);

            String mrn = item.getPatientMrn();
            if (mrn != null && !mrn.trim().isEmpty()) {
                tvPatientMrn.setVisibility(View.VISIBLE);
                tvPatientMrn.setText("MRN: " + mrn);
            } else {
                tvPatientMrn.setVisibility(View.GONE);
            }

            String docName = item.getDoctorName() != null ? item.getDoctorName() : "Doctor";
            String dept = item.getDepartment() != null && !item.getDepartment().isEmpty() ? item.getDepartment() : "General Medicine";
            tvDoctorInfo.setText(docName + " • " + dept);

            String date = item.getAppointmentDate() != null ? item.getAppointmentDate() : "Date TBD";
            String time = item.getAppointmentTime() != null ? item.getAppointmentTime() : item.getTimeSlot();
            tvAppointmentDateTime.setText(date + (time != null && !time.isEmpty() ? " • " + time : ""));

            String reason = item.getReason();
            if (reason != null && !reason.trim().isEmpty()) {
                tvAppointmentReason.setText("Reason: " + reason);
            } else {
                tvAppointmentReason.setText("Reason: General Consultation");
            }

            String status = item.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Pending";
            }
            tvAppointmentStatus.setText(status.toUpperCase());
            applyStatusBadgeStyle(tvAppointmentStatus, status);
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
            drawable.setCornerRadius(20f);
            drawable.setColor(bgColor);
            drawable.setStroke(2, textColor);
            badge.setBackground(drawable);
        }
    }
}
