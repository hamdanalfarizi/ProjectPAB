package com.example.projectpab;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvUsers;
    private TextView tvUserCount;
    private EditText etSearch;
    private Button btnLogout, btnRefresh;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        initDatabase();
        setupListeners();
        loadUsers();
    }

    private void initViews() {
        lvUsers = findViewById(R.id.lv_users);
        tvUserCount = findViewById(R.id.tv_user_count);
        etSearch = findViewById(R.id.et_search);
        btnLogout = findViewById(R.id.btn_logout);
        btnRefresh = findViewById(R.id.btn_refresh);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        userAdapter = new UserAdapter();
        lvUsers.setAdapter(userAdapter);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnRefresh.setOnClickListener(v -> loadUsers());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        userList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("users", null, null, null, null, null, "username ASC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

                userList.add(new User(id, email, username, role));
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredUserList.clear();
        filteredUserList.addAll(userList);
        userAdapter.notifyDataSetChanged();
        updateUserCount();
    }

    private void filterUsers(String query) {
        filteredUserList.clear();

        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
    }

    private void updateUserCount() {
        tvUserCount.setText("Total User: " + userList.size());
    }

    private void deleteUser(User user) {
        if (user.getEmail().equals("admin@gmail.com")) {
            Toast.makeText(this, "Admin tidak dapat dihapus!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus user: " + user.getUsername() + "?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    if (dbHelper.deleteUser(user.getEmail())) {
                        Toast.makeText(this, "User berhasil dihapus", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Gagal menghapus user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void showUserDetail(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Detail User")
                .setMessage("ID: " + user.getId() +
                        "\nEmail: " + user.getEmail() +
                        "\nUsername: " + user.getUsername() +
                        "\nRole: " + user.getRole())
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Ganti dengan MainActivity atau LoginActivity sesuai dengan project Anda
                    Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    // User Model Class
    public static class User {
        private int id;
        private String email;
        private String username;
        private String role;

        public User(int id, String email, String username, String role) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.role = role;
        }

        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }

    // Custom Adapter for User List
    private class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return filteredUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AdminActivity.this)
                        .inflate(R.layout.item_user, parent, false);
            }

            User user = filteredUserList.get(position);

            TextView tvUsername = convertView.findViewById(R.id.tv_username);
            TextView tvEmail = convertView.findViewById(R.id.tv_email);
            TextView tvRole = convertView.findViewById(R.id.tv_role);
            Button btnDetail = convertView.findViewById(R.id.btn_detail);
            Button btnDelete = convertView.findViewById(R.id.btn_delete);

            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole());

            // Set warna role
            if (user.getRole().equals("Admin")) {
                tvRole.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvRole.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }

            btnDetail.setOnClickListener(v -> showUserDetail(user));
            btnDelete.setOnClickListener(v -> deleteUser(user));

            // Disable delete button untuk admin
            if (user.getEmail().equals("admin@gmail.com")) {
                btnDelete.setEnabled(false);
                btnDelete.setAlpha(0.5f);
            } else {
                btnDelete.setEnabled(true);
                btnDelete.setAlpha(1.0f);
            }

            return convertView;
        }
    }
}