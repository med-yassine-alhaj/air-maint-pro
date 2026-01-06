package com.example.techniciens.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.techniciens.data.AssignmentWithDetails;
import com.example.techniciens.data.Intervention;
import com.example.techniciens.data.Technician;
import com.example.techniciens.data.TechnicianRepository;
import com.example.techniciens.data.TechnicianStats;

import java.util.List;

public class TechnicianViewModel extends AndroidViewModel {
    private final TechnicianRepository repository;
    private final LiveData<List<Technician>> technicians;
    private final LiveData<List<Intervention>> interventions;
    private final LiveData<List<AssignmentWithDetails>> assignments;
    private final LiveData<List<TechnicianStats>> stats;

    public TechnicianViewModel(@NonNull Application application) {
        super(application);
        repository = new TechnicianRepository(application);
        technicians = repository.getTechnicians();
        interventions = repository.getInterventions();
        assignments = repository.getAssignments();
        stats = repository.getStats();
    }

    public LiveData<List<Technician>> getTechnicians() {
        return technicians;
    }

    public LiveData<List<Intervention>> getInterventions() {
        return interventions;
    }

    public LiveData<List<AssignmentWithDetails>> getAssignments() {
        return assignments;
    }

    public LiveData<List<TechnicianStats>> getStats() {
        return stats;
    }

    public void addTechnician(String name, String specialty, int experienceYears, boolean available) {
        repository.addTechnician(name, specialty, experienceYears, available);
    }

    public void addIntervention(String title, long scheduledAt, int durationHours, String location) {
        repository.addIntervention(title, scheduledAt, durationHours, location);
    }

    public void assignTechnician(long technicianId, long interventionId, String status) {
        repository.assignTechnician(technicianId, interventionId, status);
    }

    public void addInterventionAndAssign(String title, long scheduledAt, int durationHours, String location, long technicianId, String status) {
        repository.addInterventionAndAssign(title, scheduledAt, durationHours, location, technicianId, status);
    }

    public void updateTechnician(Technician technician) {
        repository.updateTechnician(technician);
    }

    public void deleteTechnician(long technicianId) {
        repository.deleteTechnician(technicianId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
