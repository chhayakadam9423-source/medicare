package com.example.medicare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.R;
import com.example.medicare.models.Doctor;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DoctorManageAdapter extends RecyclerView.Adapter<DoctorManageAdapter.ViewHolder> {

    public interface OnDoctorActionListener {
        void onEdit(Doctor doctor);
        void onDelete(Doctor doctor);
    }

    private final Context context;
    private final List<Doctor> doctorList = new ArrayList<>();
    private final OnDoctorActionListener listener;

    public DoctorManageAdapter(Context context, OnDoctorActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> list) {
        doctorList.clear();
        if (list != null) {
            doctorList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = doctorList.get(position);
        holder.bind(doc);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDocName;
        private final TextView tvDocSpecialtyQual;
        private final TextView tvDocFee;
        private final TextView tvDocExperience;
        private final TextView tvDocDepartment;
        private final TextView tvDocHospital;
        private final TextView tvDocRoom;
        private final TextView tvDocContact;
        private final MaterialButton btnEditDoctor;
        private final MaterialButton btnDeleteDoctor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocName = itemView.findViewById(R.id.tvDocName);
            tvDocSpecialtyQual = itemView.findViewById(R.id.tvDocSpecialtyQual);
            tvDocFee = itemView.findViewById(R.id.tvDocFee);
            tvDocExperience = itemView.findViewById(R.id.tvDocExperience);
            tvDocDepartment = itemView.findViewById(R.id.tvDocDepartment);
            tvDocHospital = itemView.findViewById(R.id.tvDocHospital);
            tvDocRoom = itemView.findViewById(R.id.tvDocRoom);
            tvDocContact = itemView.findViewById(R.id.tvDocContact);
            btnEditDoctor = itemView.findViewById(R.id.btnEditDoctor);
            btnDeleteDoctor = itemView.findViewById(R.id.btnDeleteDoctor);

            btnEditDoctor.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEdit(doctorList.get(pos));
                }
            });

            btnDeleteDoctor.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(doctorList.get(pos));
                }
            });
        }

        public void bind(Doctor doc) {
            tvDocName.setText(doc.getName() != null ? doc.getName() : "Dr. Medical Officer");

            String spec = doc.getSpecialization() != null ? doc.getSpecialization() : "General";
            String qual = doc.getQualification() != null ? doc.getQualification() : "";
            tvDocSpecialtyQual.setText(qual.isEmpty() ? spec : spec + " • " + qual);

            double fee = doc.getConsultationFee();
            tvDocFee.setText("Fee: ₹" + (int) fee);

            tvDocExperience.setText("Exp: " + doc.getExperience() + " Years");

            String dept = doc.getDepartment() != null && !doc.getDepartment().isEmpty() ? doc.getDepartment() : spec;
            tvDocDepartment.setText("Dept: " + dept);

            String hosp = doc.getHospitalName() != null && !doc.getHospitalName().isEmpty() ? doc.getHospitalName() : "Medicare Hospital";
            tvDocHospital.setText(hosp);

            String room = doc.getRoomNumber() != null && !doc.getRoomNumber().isEmpty() ? doc.getRoomNumber() : "Room TBD";
            tvDocRoom.setText(room);

            String phone = doc.getPhone();
            String email = doc.getEmail();
            StringBuilder contact = new StringBuilder();
            if (phone != null && !phone.trim().isEmpty()) {
                contact.append("Phone: ").append(phone);
            }
            if (email != null && !email.trim().isEmpty()) {
                if (contact.length() > 0) contact.append(" • ");
                contact.append(email);
            }

            if (contact.length() > 0) {
                tvDocContact.setVisibility(View.VISIBLE);
                tvDocContact.setText(contact.toString());
            } else {
                tvDocContact.setVisibility(View.GONE);
            }
        }
    }
}
