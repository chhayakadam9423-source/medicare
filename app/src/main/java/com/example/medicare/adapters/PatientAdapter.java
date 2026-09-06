package com.example.medicare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicare.R;
import com.example.medicare.models.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
    private final List<Patient> patients;

    public PatientAdapter(List<Patient> patients) {
        this.patients = patients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient p = patients.get(position);
        holder.tvPatientName.setText(p.getName());
        holder.tvBloodGroup.setText(p.getBloodGroup());
        holder.tvPatientDetails.setText("Age: " + p.getAge() + " • " + p.getGender() + " • " + p.getMrn());
        holder.tvDiagnosis.setText("Diagnosis: " + p.getDiagnosis() + "\nContact: " + p.getPhone());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvBloodGroup, tvPatientDetails, tvDiagnosis;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
        }
    }
}
