package com.example.projectpab;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RegisterActivity extends AppCompatActivity {

    private TextView etRole;
    private EditText etEmail, etUsername, etPassword;
    private AppCompatButton btnRegister;
    private ImageButton btnBack;
    private TextView tvSignin;
    private String selectedRole = "Freelancer";
    private final String[] roles = {"Freelancer", "Klien"};

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        initializeDatabase();
        setupClickListeners();
    }

    private void initializeViews() {
        etRole = findViewById(R.id.textRole);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        tvSignin = findViewById(R.id.tvSignin);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupClickListeners() {
        etRole.setOnClickListener(v -> showRoleDialog());
        btnRegister.setOnClickListener(v -> handleRegister());

        // Tombol back untuk kembali ke activity sebelumnya
        btnBack.setOnClickListener(v -> {
            finish(); // Menutup activity dan kembali ke activity sebelumnya
        });

        // TextView Sign In untuk pindah ke LoginActivity
        tvSignin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Menutup RegisterActivity
        });
    }

    private void showRoleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Role")
                .setSingleChoiceItems(roles, getSelectedRoleIndex(), (dialog, which) -> {
                    selectedRole = roles[which];
                    etRole.setText(selectedRole);
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    Toast.makeText(this, "Role dipilih: " + selectedRole, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private int getSelectedRoleIndex() {
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(selectedRole)) return i;
        }
        return 0;
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi password minimal 6 karakter
        if (password.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.registerUser(email, username, password, selectedRole);
        if (success) {
            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();

            // Pindah ke LoginActivity setelah registrasi berhasil
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registrasi gagal. Email mungkin sudah digunakan.", Toast.LENGTH_SHORT).show();
        }
    }
}