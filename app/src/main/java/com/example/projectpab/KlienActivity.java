package com.example.projectpab;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KlienActivity extends AppCompatActivity {

    private TabHost tabHost;
    private TextView tvActiveJobs, tvApplications, tvCompleted;
    private TextView tvClientName, tvClientEmail, tvClientCompany;
    private ListView listMyJobs, listApplications;
    private EditText etSearchMyJobs;
    private Button btnEditProfile, btnLogout;
    private FloatingActionButton fabPostJob;
    private ImageView chatIcon;

    // Database
    private DatabaseHelper dbHelper;

    // User session
    private int currentUserId;
    private String currentUserEmail;

    // Data Lists with IDs for tracking
    private List<JobItem> myJobsList;
    private List<ApplicationItem> applicationsList;
    private ArrayAdapter<String> jobsAdapter;
    private ArrayAdapter<String> applicationsAdapter;

    private static class JobItem {
        int id;
        String title;
        String description;
        String status;
        int budget;
        String imagePath;
        String createdAt;

        @Override
        public String toString() {
            return title + "\n" + description + "\nStatus: " + status +
                    "\nBudget: Rp " + String.format("%,d", budget) +
                    (imagePath != null && !imagePath.isEmpty() ? "\n📷 Ada gambar" : "");
        }
    }

    private static class ApplicationItem {
        int id;
        int jobId;
        String jobTitle;
        String applicantName;
        String status;
        String appliedAt;

        @Override
        public String toString() {
            return "Job: " + jobTitle +
                    "\nPelamar: " + applicantName +
                    "\nStatus: " + status.toUpperCase() +
                    "\nTanggal: " + appliedAt;
        }

        public void updateStatus(String newStatus) {
            this.status = newStatus;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_klien);

        // Get user session
        getUserSession();

        // Initialize Database
        initDatabase();

        // Initialize Views
        initViews();

        // Setup TabHost
        setupTabHost();

        // Load Data
        loadData();

        // Setup Listeners
        setupListeners();
    }

    private void getUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize database to get user ID
        dbHelper = new DatabaseHelper(this);
        currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

        if (currentUserId == -1) {
            Toast.makeText(this, "User not found, please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initDatabase() {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }
    }

    private void initViews() {
        // Stats Cards
        tvActiveJobs = findViewById(R.id.tv_active_jobs);
        tvApplications = findViewById(R.id.tv_applications);
        tvCompleted = findViewById(R.id.tv_completed);

        // TabHost
        tabHost = findViewById(R.id.tabHost);

        // Profile Views
        tvClientName = findViewById(R.id.tvClientName);
        tvClientEmail = findViewById(R.id.tvClientEmail);
        tvClientCompany = findViewById(R.id.tvClientCompany);

        // ListViews
        listMyJobs = findViewById(R.id.listMyJobs);
        listApplications = findViewById(R.id.listApplications);

        // Other Views
        etSearchMyJobs = findViewById(R.id.et_search_my_jobs);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        fabPostJob = findViewById(R.id.fabPostJob);

        // Initialize Lists and Adapters
        myJobsList = new ArrayList<>();
        applicationsList = new ArrayList<>();

        // Convert object lists to string lists for adapters
        List<String> jobStrings = new ArrayList<>();
        List<String> applicationStrings = new ArrayList<>();

        jobsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jobStrings);
        applicationsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, applicationStrings);

        listMyJobs.setAdapter(jobsAdapter);
        listApplications.setAdapter(applicationsAdapter);
    }

    private void setupTabHost() {
        tabHost.setup();

        // Tab 1: My Jobs
        TabHost.TabSpec tabMyJobs = tabHost.newTabSpec("myjobs");
        tabMyJobs.setContent(R.id.tabMyJobs);
        tabMyJobs.setIndicator("Job Saya");
        tabHost.addTab(tabMyJobs);

        // Tab 2: Applications
        TabHost.TabSpec tabApplications = tabHost.newTabSpec("applications");
        tabApplications.setContent(R.id.tabApplications);
        tabApplications.setIndicator("Lamaran");
        tabHost.addTab(tabApplications);

        // Tab 3: Profile
        TabHost.TabSpec tabProfile = tabHost.newTabSpec("profile");
        tabProfile.setContent(R.id.tabProfile);
        tabProfile.setIndicator("Profile");
        tabHost.addTab(tabProfile);

        tabHost.setCurrentTab(0);
    }

    private void loadData() {
        loadUserProfile();
        loadMyJobs();
        loadApplications();
        updateStats();
    }

    private void loadUserProfile() {
        Cursor cursor = dbHelper.getAllUsers();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                if (id == currentUserId) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                    String company = cursor.getString(cursor.getColumnIndexOrThrow("company"));

                    tvClientName.setText(name != null && !name.isEmpty() ? name : "Nama belum diisi");
                    tvClientEmail.setText(email != null ? email : "Email tidak valid");
                    break;
                }
            }
            cursor.close();
        }
    }

    private void loadMyJobs() {
        myJobsList.clear();
        Cursor cursor = dbHelper.getAllJobs();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int clientId = cursor.getInt(cursor.getColumnIndexOrThrow("client_id"));

                if (clientId == currentUserId) {
                    JobItem job = new JobItem();
                    job.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    job.title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    job.description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    job.status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    job.budget = cursor.getInt(cursor.getColumnIndexOrThrow("budget"));
                    job.imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                    job.createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));

                    myJobsList.add(job);
                }
            }
            cursor.close();
        }

        updateJobsAdapter();
    }

    private void loadApplications() {
        applicationsList.clear();
        Cursor cursor = dbHelper.getAllApplications();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int jobId = cursor.getInt(cursor.getColumnIndexOrThrow("job_id"));

                // Check if this job belongs to current client
                if (isMyJob(jobId)) {
                    ApplicationItem app = new ApplicationItem();
                    app.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    app.jobId = jobId;
                    app.status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    app.appliedAt = cursor.getString(cursor.getColumnIndexOrThrow("applied_at"));

                    // Get job title and applicant name
                    app.jobTitle = getJobTitle(jobId);
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                    app.applicantName = getUserName(userId);

                    applicationsList.add(app);
                }
            }
            cursor.close();
        }

        updateApplicationsAdapter();
    }

    private void updateStats() {
        int activeCount = 0, completedCount = 0;

        for (JobItem job : myJobsList) {
            if ("active".equals(job.status)) {
                activeCount++;
            } else if ("completed".equals(job.status)) {
                completedCount++;
            }
        }

        tvActiveJobs.setText(String.valueOf(activeCount));
        tvApplications.setText(String.valueOf(applicationsList.size()));
        tvCompleted.setText(String.valueOf(completedCount));
    }

    private void updateJobsAdapter() {
        List<String> jobStrings = new ArrayList<>();
        for (JobItem job : myJobsList) {
            jobStrings.add(job.toString());
        }
        jobsAdapter.clear();
        jobsAdapter.addAll(jobStrings);
        jobsAdapter.notifyDataSetChanged();
    }

    private void updateApplicationsAdapter() {
        List<String> appStrings = new ArrayList<>();
        for (ApplicationItem app : applicationsList) {
            String statusIcon = "";
            switch (app.status.toLowerCase()) {
                case "pending":
                    statusIcon = "⏳";
                    break;
                case "accepted":
                    statusIcon = "✅";
                    break;
                case "rejected":
                    statusIcon = "❌";
                    break;
            }

            String displayText = "Job: " + app.jobTitle +
                    "\nPelamar: " + app.applicantName +
                    "\nStatus: " + statusIcon + " " + app.status.toUpperCase() +
                    "\nTanggal: " + app.appliedAt;

            appStrings.add(displayText);
        }

        applicationsAdapter.clear();
        applicationsAdapter.addAll(appStrings);
        applicationsAdapter.notifyDataSetChanged();
    }

    private boolean isMyJob(int jobId) {
        for (JobItem job : myJobsList) {
            if (job.id == jobId) {
                return true;
            }
        }
        return false;
    }

    private String getJobTitle(int jobId) {
        Cursor cursor = dbHelper.getAllJobs();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndexOrThrow("id")) == jobId) {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    cursor.close();
                    return title;
                }
            }
            cursor.close();
        }
        return "Job tidak ditemukan";
    }

    private String getUserName(int userId) {
        Cursor cursor = dbHelper.getAllUsers();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndexOrThrow("id")) == userId) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    cursor.close();
                    return name != null && !name.isEmpty() ? name : "User tanpa nama";
                }
            }
            cursor.close();
        }
        return "User tidak ditemukan";
    }

    // FIXED: Added getCurrentUserId method
    private int getCurrentUserId() {
        return currentUserId;
    }

    private void setupListeners() {
        // Search functionality
        etSearchMyJobs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchJobs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Job item click listener: buka dialog edit/hapus
        listMyJobs.setOnItemClickListener((parent, view, position, id) -> {
            if (position < myJobsList.size()) {
                JobItem selectedJob = myJobsList.get(position);
                showEditDeleteJobDialog(selectedJob);
            }
        });

        listApplications.setOnItemClickListener((parent, view, position, id) -> {
            if (position < applicationsList.size()) {
                ApplicationItem selectedApp = applicationsList.get(position);
                showApplicationActionDialog(selectedApp);
            }
        });

        // Floating Action Button - Post Job
        fabPostJob.setOnClickListener(v -> {
            Intent intent = new Intent(KlienActivity.this, PostJobActivity.class);
            startActivity(intent);
        });

        // Edit Profile Button
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(KlienActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Apakah Anda yakin ingin logout?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Logout jika pengguna menekan "Ya"
                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        finish();
                        Intent intent = new Intent(KlienActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(KlienActivity.this, "Berhasil logout", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        // Tab change listener
        tabHost.setOnTabChangedListener(tabId -> {
            switch (tabId) {
                case "myjobs":
                    loadMyJobs();
                    break;
                case "applications":
                    loadApplications();
                    break;
                case "profile":
                    loadUserProfile();
                    break;
            }
        });
    }

    private void showApplicationActionDialog(ApplicationItem application) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kelola Lamaran");

        String message = "Job: " + application.jobTitle +
                "\nPelamar: " + application.applicantName +
                "\nStatus saat ini: " + application.status.toUpperCase() +
                "\n\nPilih tindakan:";

        builder.setMessage(message);

        // Tombol ACC (Terima)
        builder.setPositiveButton("✓ TERIMA", (dialog, which) -> {
            showConfirmationDialog(application, "accepted", "menerima");
        });

        // Tombol TOLAK
        builder.setNegativeButton("✗ TOLAK", (dialog, which) -> {
            showConfirmationDialog(application, "rejected", "menolak");
        });

        // Tombol Batal
        builder.setNeutralButton("Batal", null);

        AlertDialog dialog = builder.create();

        // Ubah warna tombol setelah dialog ditampilkan
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });

        dialog.show();
    }

    private void showConfirmationDialog(ApplicationItem application, String newStatus, String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Apakah Anda yakin ingin " + action + " lamaran dari " + application.applicantName + "?");

        builder.setPositiveButton("Ya", (dialog, which) -> {
            updateApplicationStatus(application, newStatus);
        });

        builder.setNegativeButton("Tidak", null);
        builder.show();
    }

    private void updateApplicationStatus(ApplicationItem application, String newStatus) {
        boolean success = dbHelper.updateApplicationStatus(application.id, newStatus);

        if (success) {
            application.updateStatus(newStatus);
            updateApplicationsAdapter();

            String statusText = newStatus.equals("accepted") ? "diterima" : "ditolak";
            Toast.makeText(this, "Lamaran berhasil " + statusText, Toast.LENGTH_SHORT).show();

            // Kirim notifikasi ke freelancer
            String notificationTitle = "Status Lamaran";
            String notificationMessage = "Lamaran Anda untuk job '" + application.jobTitle + "' telah " + statusText;
            long timestamp = System.currentTimeMillis();

            String clientName = getUserName(getCurrentUserId());
            if (clientName != null) {
                notificationMessage += " oleh " + clientName;
            }

            int freelancerId = dbHelper.getApplicantIdForApplication(application.id);
            if (freelancerId != -1) {
                dbHelper.insertNotification(
                        String.valueOf(freelancerId),
                        notificationTitle,
                        notificationMessage,
                        timestamp
                );
            }
        } else {
            Toast.makeText(this, "Gagal mengubah status lamaran", Toast.LENGTH_SHORT).show();
        }
    }
    private void showEditDeleteJobDialog(JobItem job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Or Delete Job");
        builder.setMessage("Select an action for '" + job.title + "'");

        builder.setPositiveButton("Edit", (dialog, which) -> showEditJobDialog(job));
        builder.setNegativeButton("Delete", (dialog, which) -> showDeleteJobConfirmation(job));
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showEditJobDialog(JobItem job) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_job, null);
        EditText etTitle = dialogView.findViewById(R.id.etEditJobTitle);
        EditText etDescription = dialogView.findViewById(R.id.etEditJobDescription);
        EditText etBudget = dialogView.findViewById(R.id.etEditJobBudget);

        etTitle.setText(job.title);
        etDescription.setText(job.description);
        etBudget.setText(String.valueOf(job.budget));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Job");
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDescription = etDescription.getText().toString().trim();
            String budgetStr = etBudget.getText().toString().trim();

            if (newTitle.isEmpty() || newDescription.isEmpty() || budgetStr.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            int newBudget;
            try {
                newBudget = Integer.parseInt(budgetStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Budget harus berupa angka", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateJob(job.id, newTitle, newDescription, newBudget, job.imagePath);

            if (updated) {
                Toast.makeText(this, "Job berhasil diperbarui", Toast.LENGTH_SHORT).show();
                loadMyJobs();
                updateStats();
            } else {
                Toast.makeText(this, "Gagal memperbarui job", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteJobConfirmation(JobItem job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete '" + job.title + "'?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            boolean deleted = dbHelper.deleteJob(job.id);
            if (deleted) {
                Toast.makeText(this, "Job berhasil dihapus", Toast.LENGTH_SHORT).show();
                loadMyJobs();
                updateStats();
            } else {
                Toast.makeText(this, "Gagal menghapus job", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void searchJobs(String query) {
        if (query.trim().isEmpty()) {
            updateJobsAdapter();
            return;
        }

        List<String> filteredJobs = new ArrayList<>();
        for (JobItem job : myJobsList) {
            if (job.title.toLowerCase().contains(query.toLowerCase()) ||
                    job.description.toLowerCase().contains(query.toLowerCase())) {
                filteredJobs.add(job.toString());
            }
        }

        ArrayAdapter<String> filteredAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, filteredJobs);
        listMyJobs.setAdapter(filteredAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh data ketika kembali ke activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}