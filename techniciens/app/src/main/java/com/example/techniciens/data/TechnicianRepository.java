package com.example.techniciens.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TechnicianRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<Technician>> techniciansLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Intervention>> interventionsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AssignmentWithDetails>> assignmentsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<TechnicianStats>> statsLive = new MutableLiveData<>(new ArrayList<>());

    private final List<Technician> currentTechnicians = new ArrayList<>();
    private final List<Intervention> currentInterventions = new ArrayList<>();
    private final List<Assignment> currentAssignments = new ArrayList<>();

    private ListenerRegistration technicianListener;
    private ListenerRegistration interventionListener;
    private ListenerRegistration assignmentListener;

    public TechnicianRepository(Application application) {
        subscribeTechnicians();
        subscribeInterventions();
        subscribeAssignments();
    }

    public LiveData<List<Technician>> getTechnicians() {
        return techniciansLive;
    }

    public LiveData<List<Intervention>> getInterventions() {
        return interventionsLive;
    }

    public LiveData<List<AssignmentWithDetails>> getAssignments() {
        return assignmentsLive;
    }

    public LiveData<List<TechnicianStats>> getStats() {
        return statsLive;
    }

    public void addTechnician(String name, String specialty, int experienceYears, boolean available) {
        long id = System.currentTimeMillis();
        Technician technician = new Technician(name, specialty, experienceYears, available);
        technician.id = id;
        firestore.collection("technicians").document(String.valueOf(id)).set(technician);
    }

    public void addIntervention(String title, long scheduledAt, int durationHours, String location) {
        long id = System.currentTimeMillis();
        Intervention intervention = new Intervention(title, scheduledAt, durationHours, location);
        intervention.id = id;
        firestore.collection("interventions").document(String.valueOf(id)).set(intervention);
    }

    public void assignTechnician(long technicianId, long interventionId, String status) {
        long id = System.currentTimeMillis();
        Assignment assignment = new Assignment(technicianId, interventionId, System.currentTimeMillis(), status);
        assignment.id = id;
        firestore.collection("assignments").document(String.valueOf(id)).set(assignment);
    }

    public void addInterventionAndAssign(String title, long scheduledAt, int durationHours, String location, long technicianId, String status) {
        long interventionId = System.currentTimeMillis();
        Intervention intervention = new Intervention(title, scheduledAt, durationHours, location);
        intervention.id = interventionId;
        Task<Void> addInterventionTask = firestore.collection("interventions").document(String.valueOf(interventionId)).set(intervention);
        addInterventionTask.addOnSuccessListener(unused -> assignTechnician(technicianId, interventionId, status));
    }

    public void updateTechnician(Technician technician) {
        if (technician == null || technician.id == 0) return;
        firestore.collection("technicians")
                .document(String.valueOf(technician.id))
                .set(technician);
    }

    public void deleteTechnician(long technicianId) {
        if (technicianId == 0) return;
        firestore.collection("technicians").document(String.valueOf(technicianId)).delete();
        firestore.collection("assignments")
                .whereEqualTo("technicianId", technicianId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null) return;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    private void subscribeTechnicians() {
        technicianListener = firestore.collection("technicians").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            List<Technician> list = new ArrayList<>();
            for (DocumentSnapshot doc : value.getDocuments()) {
                Technician t = doc.toObject(Technician.class);
                if (t != null) {
                    // Ensure id is set from document id if missing
                    if (t.id == 0) {
                        try {
                            t.id = Long.parseLong(doc.getId());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    list.add(t);
                }
            }
            currentTechnicians.clear();
            currentTechnicians.addAll(list);
            techniciansLive.setValue(list);
            recomputeAssignments();
            recomputeStats();
        });
    }

    private void subscribeInterventions() {
        interventionListener = firestore.collection("interventions").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            List<Intervention> list = new ArrayList<>();
            for (DocumentSnapshot doc : value.getDocuments()) {
                Intervention i = doc.toObject(Intervention.class);
                if (i != null) {
                    if (i.id == 0) {
                        try {
                            i.id = Long.parseLong(doc.getId());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    list.add(i);
                }
            }
            currentInterventions.clear();
            currentInterventions.addAll(list);
            interventionsLive.setValue(list);
            recomputeAssignments();
            recomputeStats();
        });
    }

    private void subscribeAssignments() {
        assignmentListener = firestore.collection("assignments").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            List<Assignment> list = new ArrayList<>();
            for (DocumentSnapshot doc : value.getDocuments()) {
                Assignment a = doc.toObject(Assignment.class);
                if (a != null) {
                    if (a.id == 0) {
                        try {
                            a.id = Long.parseLong(doc.getId());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    list.add(a);
                }
            }
            currentAssignments.clear();
            currentAssignments.addAll(list);
            recomputeAssignments();
            recomputeStats();
        });
    }

    private void recomputeAssignments() {
        Map<Long, Technician> techById = new HashMap<>();
        for (Technician t : currentTechnicians) {
            techById.put(t.id, t);
        }

        Map<Long, Intervention> interventionById = new HashMap<>();
        for (Intervention i : currentInterventions) {
            interventionById.put(i.id, i);
        }

        List<AssignmentWithDetails> detailed = new ArrayList<>();
        for (Assignment a : currentAssignments) {
            AssignmentWithDetails details = new AssignmentWithDetails();
            details.assignment = a;
            details.technician = techById.get(a.technicianId);
            details.intervention = interventionById.get(a.interventionId);
            detailed.add(details);
        }
        assignmentsLive.setValue(detailed);
    }

    private void recomputeStats() {
        Map<Long, TechnicianStats> statsMap = new HashMap<>();
        for (Assignment a : currentAssignments) {
            TechnicianStats stat = statsMap.getOrDefault(a.technicianId, new TechnicianStats(a.technicianId, 0, 0));
            if (Objects.equals(a.status, "done")) {
                stat.interventionsDone += 1;
                Intervention intervention = null;
                for (Intervention i : currentInterventions) {
                    if (i.id == a.interventionId) {
                        intervention = i;
                        break;
                    }
                }
                if (intervention != null) {
                    stat.hoursWorked += intervention.durationHours;
                }
            }
            statsMap.put(a.technicianId, stat);
        }
        statsLive.setValue(new ArrayList<>(statsMap.values()));
    }

    public void cleanup() {
        if (technicianListener != null) technicianListener.remove();
        if (interventionListener != null) interventionListener.remove();
        if (assignmentListener != null) assignmentListener.remove();
    }
}
