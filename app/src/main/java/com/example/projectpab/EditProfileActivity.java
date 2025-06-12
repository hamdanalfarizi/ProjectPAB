package com.example.projectpab;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "profile_data";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_HOBBY = "hobby";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_BIRTH_DATE = "birth_date";
    private static final String KEY_BIRTH_TIME = "birth_time";

    private EditText etFullName, etEmail, etHobby, etPhone, etAddress, etDate, etTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etHobby = findViewById(R.id.etHobby);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);

        // Set click listeners for date and time pickers
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // Save button click listener
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveProfile());

        // Load existing profile data
        loadProfileData();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    etDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    etTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveProfile() {
        // Get all field values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String hobby = etHobby.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthDate = etDate.getText().toString().trim();
        String birthTime = etTime.getText().toString().trim();

        // Validate required fields
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (hobby.isEmpty()) {
            etHobby.setError("Hobby is required");
            etHobby.requestFocus();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_HOBBY, hobby);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_BIRTH_DATE, birthDate);
        editor.putString(KEY_BIRTH_TIME, birthTime);

        editor.apply();

        // Log saved data for debugging
        Log.d("ProfileData", "Saved: " + fullName + ", " + email + ", " + hobby);

        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();

        // Set result and finish
        setResult(RESULT_OK);
        finish();
    }

    private void loadProfileData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        etFullName.setText(preferences.getString(KEY_FULL_NAME, ""));
        etEmail.setText(preferences.getString(KEY_EMAIL, ""));
        etHobby.setText(preferences.getString(KEY_HOBBY, ""));
        etPhone.setText(preferences.getString(KEY_PHONE, ""));
        etAddress.setText(preferences.getString(KEY_ADDRESS, ""));
        etDate.setText(preferences.getString(KEY_BIRTH_DATE, ""));
        etTime.setText(preferences.getString(KEY_BIRTH_TIME, ""));
    }
}