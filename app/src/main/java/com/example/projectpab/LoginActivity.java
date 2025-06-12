package com.example.projectpab;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView etEmail;
    private TextView tvForgotPassword;
    private EditText etPassword;
    private AppCompatButton btnLogin;
    private CheckBox checkBoxRemember;
    private TextView tvSignup;
    private String selectedRole = "Freelancer";
    private final String[] roles = {"Freelancer", "Klien", "Admin"};
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "login_pref";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeDatabase();
        initializeSharedPreferences();
        loadSavedCredentials();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignup = findViewById(R.id.tvSignup);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    private void loadSavedCredentials() {
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);

        if (isRemembered) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            String savedRole = sharedPreferences.getString(KEY_ROLE, "Freelancer");

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
            selectedRole = savedRole;
            checkBoxRemember.setChecked(true);

            // Update tampilan role
            ((TextView) findViewById(R.id.textRole)).setText(selectedRole);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.textRole).setOnClickListener(v -> showRoleDialog());
        btnLogin.setOnClickListener(v -> handleLogin());
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void showRoleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Role")
                .setSingleChoiceItems(roles, getSelectedRoleIndex(), (dialog, which) -> selectedRole = roles[which])
                .setPositiveButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Role dipilih: " + selectedRole, Toast.LENGTH_SHORT).show();

                    //Update tampilan role
                    ((TextView) findViewById(R.id.textRole)).setText(selectedRole);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    //forget password



    private int getSelectedRoleIndex() {
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(selectedRole)) return i;
        }
        return 0;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(email, password, selectedRole)) {
            // Simpan atau hapus credentials berdasarkan checkbox (untuk Remember Me)
            saveCredentials(email, password);

            // TAMBAHAN: Buat session untuk activity tujuan
            createUserSession(email, selectedRole);

            Toast.makeText(this, "Login berhasil sebagai " + selectedRole, Toast.LENGTH_SHORT).show();

            Intent intent = getActivityIntent();
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email, password, atau role salah", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (checkBoxRemember.isChecked()) {
            // Simpan credentials
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
            editor.putString(KEY_ROLE, selectedRole);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            // Hapus credentials
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
            editor.remove(KEY_ROLE);
            editor.putBoolean(KEY_REMEMBER, false);
        }

        editor.apply();
    }

    private Intent getActivityIntent() {
        switch (selectedRole) {
            case "Admin":
                return new Intent(LoginActivity.this, AdminActivity.class);
            case "Freelancer":
                return new Intent(LoginActivity.this, MainActivity.class);
            case "Klien":
                return new Intent(LoginActivity.this, KlienActivity.class);
            default:
                return new Intent(LoginActivity.this, MainActivity.class); // fallback
        }
    }
    // METODE BARU: Buat session untuk activity tujuan
    private void createUserSession(String email, String role) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor sessionEditor = userSession.edit();

        sessionEditor.putString("email", email);
        sessionEditor.putString("role", role);
        sessionEditor.putBoolean("isLoggedIn", true);

        // Tambahan: simpan user ID untuk kemudahan akses
        int userId = databaseHelper.getUserIdByEmail(email);
        sessionEditor.putInt("userId", userId);

        sessionEditor.apply();
    }
}