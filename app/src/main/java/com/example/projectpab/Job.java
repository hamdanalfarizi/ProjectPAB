package com.example.projectpab;

public class Job {
    private int id;
    private String title;
    private String description;
    private int budget;
    private String status;
    private String createdAt;

    // Setter
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setBudget(int budget) { this.budget = budget; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // (Optional) Getter
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getBudget() { return budget; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
