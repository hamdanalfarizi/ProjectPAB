// NotificationItem.java
package com.example.projectpab;

public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;

    public NotificationItem(String id, String title, String message, long timestamp, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getter methods
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
}