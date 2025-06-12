package com.example.projectpab;

public class ApplicationItem {
    public int id;
    public String jobTitle; // Pastikan ada field ini
    public String status;

    // Constructor dan method lainnya
    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
