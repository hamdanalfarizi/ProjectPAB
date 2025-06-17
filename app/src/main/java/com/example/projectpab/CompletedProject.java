package com.example.projectpab;

public class CompletedProject {
    private int jobId;
    private String jobTitle;
    private String completionDate;
    private String freelancerName;
    private double rating;

    public CompletedProject(int jobId, String jobTitle, String completionDate, String freelancerName, double rating) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.completionDate = completionDate;
        this.freelancerName = freelancerName;
        this.rating = rating;
    }

    // Getter methods
    public int getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public String getCompletionDate() { return completionDate; }
    public String getFreelancerName() { return freelancerName; }
    public double getRating() { return rating; }
}
