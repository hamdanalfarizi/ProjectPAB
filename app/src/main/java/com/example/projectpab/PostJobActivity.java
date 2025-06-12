package com.example.projectpab;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PostJobActivity extends AppCompatActivity {

    private EditText etJobTitle, etJobDescription, etJobBudget;
    private Button btnPostJob, btnCancel;

    private DatabaseHelper dbHelper;
    private int currentUserId;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        // Get user session
        getUserSession();

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void getUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database to get user ID
        dbHelper = new DatabaseHelper(this);
        currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

        if (currentUserId == -1) {
            Toast.makeText(this, "User not found, please login again", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        etJobTitle = findViewById(R.id.et_job_title);
        etJobDescription = findViewById(R.id.et_job_description);
        etJobBudget = findViewById(R.id.et_job_budget);
        btnPostJob = findViewById(R.id.btn_post_job);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupListeners() {
        btnPostJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJob();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void postJob() {
        String title = etJobTitle.getText().toString().trim();
        String description = etJobDescription.getText().toString().trim();
        String budgetStr = etJobBudget.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            etJobTitle.setError("Title is required");
            etJobTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etJobDescription.setError("Description is required");
            etJobDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(budgetStr)) {
            etJobBudget.setError("Budget is required");
            etJobBudget.requestFocus();
            return;
        }

        int budget;
        try {
            budget = Integer.parseInt(budgetStr);
            if (budget <= 0) {
                etJobBudget.setError("Budget must be greater than 0");
                etJobBudget.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etJobBudget.setError("Please enter a valid number");
            etJobBudget.requestFocus();
            return;
        }

        // Add job to database
        boolean success = dbHelper.addJob(title, description, currentUserId, budget);

        if (success) {
            Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to KlienActivity
        } else {
            Toast.makeText(this, "Failed to post job. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}