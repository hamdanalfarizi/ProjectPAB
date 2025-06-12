package com.example.projectpab;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnReset;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnReset);

        databaseHelper = new DatabaseHelper(this);

        btnReset.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updatePassword(email, newPassword);
        if (success) {
            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }
}
