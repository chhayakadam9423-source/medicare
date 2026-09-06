package com.example.medicare.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.BookAppointmentActivity;
import com.example.medicare.R;
import com.example.medicare.models.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
    private final List<Doctor> doctors;

    public DoctorAdapter(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = doctors.get(position);
        holder.tvDocName.setText(doc.getName());
        holder.tvDocSpecialty.setText(doc.getSpecialization() + " • " + doc.getQualification());

        String feeStr = doc.getConsultationFee() > 0 ? " • Fee: ₹" + (int) doc.getConsultationFee() : "";
        holder.tvDocExperience.setText(doc.getExperience() + " Yrs Exp • " + doc.getHospitalName() + feeStr);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.example.medicare.DoctorDetailsActivity.class);
            intent.putExtra("doctor_id", doc.getId());
            intent.putExtra("doctor_name", doc.getName());
            intent.putExtra("department", doc.getDepartment());
            intent.putExtra("consultation_fee", doc.getConsultationFee());
            v.getContext().startActivity(intent);
        });

        holder.btnBookDoc.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookAppointmentActivity.class);
            intent.putExtra("doctor_id", doc.getId());
            intent.putExtra("doctor_name", doc.getName());
            intent.putExtra("department", doc.getDepartment());
            intent.putExtra("consultation_fee", doc.getConsultationFee());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctors != null ? doctors.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocName, tvDocSpecialty, tvDocExperience;
        Button btnBookDoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocName = itemView.findViewById(R.id.tvDocName);
            tvDocSpecialty = itemView.findViewById(R.id.tvDocSpecialty);
            tvDocExperience = itemView.findViewById(R.id.tvDocExperience);
            btnBookDoc = itemView.findViewById(R.id.btnBookDoc);
        }
    }
}
