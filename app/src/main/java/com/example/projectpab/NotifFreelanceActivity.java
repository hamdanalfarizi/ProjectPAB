// NotifFreelanceActivity.java
package com.example.projectpab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.widget.ImageButton;

public class NotifFreelanceActivity extends AppCompatActivity {
    private ListView listNotifications;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_freelance);

        listNotifications = findViewById(R.id.listNotifications);
        dbHelper = new DatabaseHelper(this);

        // Tombol back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Get current user ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPreferences.getString("email", "");
        currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

        loadNotifications();
    }

    private void loadNotifications() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<NotificationItem> notifications = dbHelper.getNotificationsForUser(String.valueOf(currentUserId));

        // Create and set adapter
        NotificationAdapter adapter = new NotificationAdapter(this, notifications);
        listNotifications.setAdapter(adapter);

        // Mark notifications as read when clicked
        listNotifications.setOnItemClickListener((parent, view, position, id) -> {
            NotificationItem notification = notifications.get(position);
            if (!notification.isRead()) {
                dbHelper.markNotificationAsRead(notification.getId());
                loadNotifications(); // Refresh list
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}