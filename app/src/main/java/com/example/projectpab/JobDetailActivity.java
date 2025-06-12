package com.example.projectpab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class JobDetailActivity extends AppCompatActivity {

    private TextView tvJobTitle, tvJobDescription, tvBudget, tvClientName, tvCompany;
    private Button btnBack, btnApply;

    private DatabaseHelper dbHelper;

    // Job data
    private int jobId;
    private String jobTitle;
    private String jobDescription;
    private int jobBudget;
    private String clientName;
    private String company;
    private int clientId;

    // Current user data
    private int currentUserId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Initialize views
        initViews();

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Get user session
        getUserSession();

        // Get job data from intent
        getJobDataFromIntent();

        // Display job data
        displayJobData();

        // Setup listeners
        setupListeners();

        // Check if user already applied
        checkApplicationStatus();
    }

    private void initViews() {
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvBudget = findViewById(R.id.tvBudget);
        tvClientName = findViewById(R.id.tvClientName);
        tvCompany = findViewById(R.id.tvCompany);
        btnBack = findViewById(R.id.btnBack);
        btnApply = findViewById(R.id.btnApply);
    }

    private void getUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPreferences.getString("email", "");
        currentUserRole = sharedPreferences.getString("role", "");

        if (!currentUserEmail.isEmpty()) {
            currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

            // Fallback: get role from DB if empty
            if (currentUserRole == null || currentUserRole.isEmpty()) {
                currentUserRole = dbHelper.getUserRoleByEmail(currentUserEmail);
            }
        }

        Log.d("JOB_DETAIL", "User ID: " + currentUserId);
        Log.d("JOB_DETAIL", "User Role: " + currentUserRole);
    }

    private void getJobDataFromIntent() {
        Intent intent = getIntent();
        jobId = intent.getIntExtra("job_id", -1);
        jobTitle = intent.getStringExtra("job_title");
        jobDescription = intent.getStringExtra("job_description");
        jobBudget = intent.getIntExtra("job_budget", 0);
        clientName = intent.getStringExtra("client_name");
        company = intent.getStringExtra("company");
        clientId = intent.getIntExtra("client_id", -1);

        // If data not passed through intent, try to get from database
        if (jobId != -1 && (jobTitle == null || jobTitle.isEmpty())) {
            loadJobFromDatabase();
        }
    }

    private void loadJobFromDatabase() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllJobs();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    if (id == jobId) {
                        jobTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        jobDescription = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                        jobBudget = cursor.getInt(cursor.getColumnIndexOrThrow("budget"));
                        clientId = cursor.getInt(cursor.getColumnIndexOrThrow("client_id"));

                        // Get client info
                        loadClientInfo();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading job details", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void loadClientInfo() {
        if (clientId == -1) return;

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllUsers();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    if (id == clientId) {
                        clientName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        company = cursor.getString(cursor.getColumnIndexOrThrow("company"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void displayJobData() {
        if (tvJobTitle != null) {
            tvJobTitle.setText(jobTitle != null ? jobTitle : "No Title");
        }

        if (tvJobDescription != null) {
            tvJobDescription.setText(jobDescription != null ? jobDescription : "No Description");
        }

        if (tvBudget != null) {
            tvBudget.setText("Rp " + String.format("%,d", jobBudget));
        }

        if (tvClientName != null) {
            tvClientName.setText(clientName != null ? clientName : "Unknown Client");
        }

        if (tvCompany != null) {
            tvCompany.setText(company != null ? company : "");
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // Close activity and return to previous screen
            });
        }
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                handleApplyButtonClick();
            });
        }
    }

    private void checkApplicationStatus() {
        if (currentUserId == -1 || jobId == -1) return;

        // Check if user is not a freelancer
        if (!"freelancer".equalsIgnoreCase(currentUserRole)) {
            btnApply.setVisibility(View.GONE);
            return;
        }

        // Check if user already applied for this job
        boolean hasApplied = hasUserAppliedForJob();

        if (hasApplied) {
            btnApply.setText("Already Applied");
            btnApply.setEnabled(false);
            btnApply.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else {
            btnApply.setText("Apply for this Job");
            btnApply.setEnabled(true);
            btnApply.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
        }
    }

    private boolean hasUserAppliedForJob() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllApplications();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int appJobId = cursor.getInt(cursor.getColumnIndexOrThrow("job_id"));
                    int appUserId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));

                    if (appJobId == jobId && appUserId == currentUserId) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private void handleApplyButtonClick() {
        if (currentUserId == -1 || jobId == -1) {
            Toast.makeText(this, "Unable to apply. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"freelancer".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(this, "Only freelancers can apply for jobs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check again if user already applied (double check)
        if (hasUserAppliedForJob()) {
            Toast.makeText(this, "You have already applied for this job", Toast.LENGTH_SHORT).show();
            return;
        }

        // Apply for the job
        boolean success = dbHelper.addApplication(jobId, currentUserId);

        if (success) {
            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show();

            // Update button state
            btnApply.setText("Already Applied");
            btnApply.setEnabled(false);
            btnApply.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        } else {
            Toast.makeText(this, "Failed to submit application. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // DatabaseHelper will handle database closing
    }

    // Optional: Method to refresh job data
    private void refreshJobData() {
        if (jobId != -1) {
            loadJobFromDatabase();
            displayJobData();
            checkApplicationStatus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh application status when returning to activity
        checkApplicationStatus();
    }
}