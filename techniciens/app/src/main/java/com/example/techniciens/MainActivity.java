package com.example.techniciens;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.techniciens.data.AssignmentWithDetails;
import com.example.techniciens.data.Technician;
import com.example.techniciens.data.TechnicianStats;
import com.example.techniciens.databinding.ActivityMainBinding;
import com.example.techniciens.ui.PlanningAdapter;
import com.example.techniciens.ui.TechnicianAdapter;
import com.example.techniciens.ui.TechnicianViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TechnicianAdapter.TechnicianActionListener {

    private ActivityMainBinding binding;
    private TechnicianViewModel viewModel;
    private TechnicianAdapter technicianAdapter;
    private PlanningAdapter planningAdapter;
    private final List<Technician> currentTechnicians = new ArrayList<>();
    private final List<AssignmentWithDetails> currentAssignments = new ArrayList<>();
    private List<TechnicianStats> currentStats = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        setupLists();
        setupViewModel();
        setupActions();
        setupFilters();
    }

    private void setupLists() {
        technicianAdapter = new TechnicianAdapter(this);
        planningAdapter = new PlanningAdapter();

        binding.recyclerTechnicians.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTechnicians.setAdapter(technicianAdapter);
        binding.recyclerTechnicians.setNestedScrollingEnabled(false);

        binding.recyclerPlanning.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPlanning.setAdapter(planningAdapter);
        binding.recyclerPlanning.setNestedScrollingEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TechnicianViewModel.class);

        viewModel.getTechnicians().observe(this, technicians -> {
            currentTechnicians.clear();
            if (technicians != null) {
                currentTechnicians.addAll(technicians);
            }
            applyFilters();
        });

        viewModel.getAssignments().observe(this, assignments -> {
            currentAssignments.clear();
            if (assignments != null) {
                currentAssignments.addAll(assignments);
            }
            planningAdapter.submitList(assignments);
        });

        viewModel.getStats().observe(this, stats -> {
            currentStats = stats;
            technicianAdapter.setStats(stats);
            updateStatsText();
        });
    }

    private void setupActions() {
        binding.fabAddTechnician.setOnClickListener(v -> showAddTechnicianDialog());
        binding.fabAddAssignment.setOnClickListener(v -> showAssignmentDialog(null));
    }

    private void setupFilters() {
        if (binding.inputSearch != null) {
            binding.inputSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (binding.spinnerAvailability != null) {
            binding.spinnerAvailability.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }
    }

    private void showAddTechnicianDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_technician, null, false);
        TextInputEditText inputName = dialogView.findViewById(R.id.inputName);
        TextInputEditText inputSpecialty = dialogView.findViewById(R.id.inputSpecialty);
        TextInputEditText inputExperience = dialogView.findViewById(R.id.inputExperience);
        MaterialCheckBox checkAvailability = dialogView.findViewById(R.id.checkAvailability);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_technician)
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String name = inputName.getText() != null ? inputName.getText().toString().trim() : "";
                    String specialty = inputSpecialty.getText() != null ? inputSpecialty.getText().toString().trim() : "";
                    String experienceText = inputExperience.getText() != null ? inputExperience.getText().toString().trim() : "0";
                    boolean available = checkAvailability.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(specialty)) {
                        Toast.makeText(this, "Champs obligatoires", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int experienceYears;
                    try {
                        experienceYears = Integer.parseInt(experienceText);
                    } catch (NumberFormatException e) {
                        experienceYears = 0;
                    }

                    viewModel.addTechnician(name, specialty, experienceYears, available);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditTechnicianDialog(Technician technician) {
        if (technician == null) return;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_technician, null, false);
        TextInputEditText inputName = dialogView.findViewById(R.id.inputName);
        TextInputEditText inputSpecialty = dialogView.findViewById(R.id.inputSpecialty);
        TextInputEditText inputExperience = dialogView.findViewById(R.id.inputExperience);
        MaterialCheckBox checkAvailability = dialogView.findViewById(R.id.checkAvailability);

        inputName.setText(technician.name);
        inputSpecialty.setText(technician.specialty);
        inputExperience.setText(String.valueOf(technician.experienceYears));
        checkAvailability.setChecked(technician.available);

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_technician)
                .setView(dialogView)
                .setPositiveButton(R.string.update_technician, (dialog, which) -> {
                    String name = inputName.getText() != null ? inputName.getText().toString().trim() : "";
                    String specialty = inputSpecialty.getText() != null ? inputSpecialty.getText().toString().trim() : "";
                    String experienceText = inputExperience.getText() != null ? inputExperience.getText().toString().trim() : "0";
                    boolean available = checkAvailability.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(specialty)) {
                        Toast.makeText(this, "Champs obligatoires", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int experienceYears;
                    try {
                        experienceYears = Integer.parseInt(experienceText);
                    } catch (NumberFormatException e) {
                        experienceYears = 0;
                    }

                    technician.name = name;
                    technician.specialty = specialty;
                    technician.experienceYears = experienceYears;
                    technician.available = available;
                    viewModel.updateTechnician(technician);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAssignmentDialog(@Nullable Technician preselected) {
        if (currentTechnicians.isEmpty()) {
            Toast.makeText(this, "Ajoutez d'abord un technicien", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null, false);
        Spinner spinnerTech = dialogView.findViewById(R.id.spinnerTechnician);
        TextInputEditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        TextInputEditText inputLocation = dialogView.findViewById(R.id.inputLocation);
        TextInputEditText inputDate = dialogView.findViewById(R.id.inputDate);
        TextInputEditText inputDuration = dialogView.findViewById(R.id.inputDuration);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        List<String> names = currentTechnicians.stream().map(t -> t.name).collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTech.setAdapter(adapter);

        if (preselected != null) {
            int index = currentTechnicians.indexOf(preselected);
            if (index >= 0) {
                spinnerTech.setSelection(index);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_assignment)
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String title = inputTitle.getText() != null ? inputTitle.getText().toString().trim() : "";
                    String location = inputLocation.getText() != null ? inputLocation.getText().toString().trim() : "";
                    String dateText = inputDate.getText() != null ? inputDate.getText().toString().trim() : "";
                    String durationText = inputDuration.getText() != null ? inputDuration.getText().toString().trim() : "";
                    String status = (String) spinnerStatus.getSelectedItem();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dateText)) {
                        Toast.makeText(this, "Titre et date requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long dateMillis = parseDateToMillis(dateText);
                    int durationHours = parseDuration(durationText);
                    int selectedTechIndex = spinnerTech.getSelectedItemPosition();
                    long technicianId = currentTechnicians.get(selectedTechIndex).id;

                    viewModel.addInterventionAndAssign(title, dateMillis, durationHours, location, technicianId, status);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private long parseDateToMillis(String input) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = format.parse(input);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException ignored) {
        }
        return System.currentTimeMillis();
    }

    private int parseDuration(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void updateStatsText() {
        if (currentStats == null || currentStats.isEmpty()) {
            binding.textStats.setText(getString(R.string.section_stats) + "\n" + getString(R.string.empty_state));
            return;
        }
        int totalInterventions = 0;
        int totalHours = 0;
        for (TechnicianStats stat : currentStats) {
            totalInterventions += stat.interventionsDone;
            totalHours += stat.hoursWorked;
        }
        String summary = getString(R.string.section_stats) + "\nTotal interventions : " + totalInterventions + " • Heures travaillées : " + totalHours;
        binding.textStats.setText(summary);
    }

    @Override
    public void onAssign(Technician technician) {
        showAssignmentDialog(technician);
    }

    @Override
    public void onEdit(Technician technician) {
        showEditTechnicianDialog(technician);
    }

    @Override
    public void onDelete(Technician technician) {
        if (technician == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_technician)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete_technician, (d, w) -> viewModel.deleteTechnician(technician.id))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void applyFilters() {
        String query = binding.inputSearch != null && binding.inputSearch.getText() != null
                ? binding.inputSearch.getText().toString().toLowerCase(Locale.getDefault())
                : "";

        String filter = binding.spinnerAvailability != null && binding.spinnerAvailability.getSelectedItem() != null
                ? binding.spinnerAvailability.getSelectedItem().toString()
                : getString(R.string.all_label);

        List<Technician> filtered = new ArrayList<>();
        for (Technician t : currentTechnicians) {
            boolean matchesQuery = query.isEmpty() ||
                    (t.name != null && t.name.toLowerCase(Locale.getDefault()).contains(query)) ||
                    (t.specialty != null && t.specialty.toLowerCase(Locale.getDefault()).contains(query));

            boolean matchesAvailability;
            if (filter.equals(getString(R.string.available_label))) {
                matchesAvailability = t.available;
            } else if (filter.equals(getString(R.string.busy_label))) {
                matchesAvailability = !t.available;
            } else {
                matchesAvailability = true;
            }

            if (matchesQuery && matchesAvailability) {
                filtered.add(t);
            }
        }
        technicianAdapter.submitList(filtered);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share_qr) {
            showPlanningQrDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPlanningQrDialog() {
        if (currentAssignments.isEmpty()) {
            Toast.makeText(this, "Aucune planification à partager", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = buildPlanningPayload();
        Bitmap qrBitmap = createQrBitmap(content, 900);
        if (qrBitmap == null) {
            Toast.makeText(this, "Impossible de générer le QR", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(qrBitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
                .setTitle(R.string.qr_dialog_title)
                .setView(imageView)
                .setPositiveButton(R.string.qr_dialog_close, null)
                .show();
    }

    private String buildPlanningPayload() {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(currentAssignments.size(), 25);
        for (int i = 0; i < limit; i++) {
            AssignmentWithDetails item = currentAssignments.get(i);
            String tech = item.technician != null ? item.technician.name : "";
            String title = item.intervention != null ? item.intervention.title : "";
            long date = item.intervention != null ? item.intervention.scheduledAt : 0;
            sb.append(title).append("|").append(tech).append("|").append(date).append(";");
        }
        return sb.toString();
    }

    private Bitmap createQrBitmap(String content, int size) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}