package com.example.projectpab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Flance.db";
    private static final int DATABASE_VERSION = 3;

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COMPANY = "company";

    // Table Jobs
    private static final String TABLE_JOBS = "jobs";
    private static final String COLUMN_JOB_ID = "id";
    private static final String COLUMN_JOB_TITLE = "title";
    private static final String COLUMN_JOB_DESCRIPTION = "description";
    private static final String COLUMN_CLIENT_ID = "client_id";
    private static final String COLUMN_JOB_STATUS = "status";
    private static final String COLUMN_BUDGET = "budget";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_JOB_IMAGE = "image_path";

    // Table Applications
    private static final String TABLE_APPLICATIONS = "applications";
    private static final String COLUMN_APP_ID = "id";
    private static final String COLUMN_JOB_ID_FK = "job_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_APP_STATUS = "status";
    private static final String COLUMN_APPLIED_AT = "applied_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT, " +
                COLUMN_COMPANY + " TEXT)";
        db.execSQL(createUsersTable);

        String createJobsTable = "CREATE TABLE " + TABLE_JOBS + " (" +
                COLUMN_JOB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_JOB_TITLE + " TEXT NOT NULL, " +
                COLUMN_JOB_DESCRIPTION + " TEXT, " +
                COLUMN_CLIENT_ID + " INTEGER, " +
                COLUMN_JOB_STATUS + " TEXT DEFAULT 'active', " +
                COLUMN_BUDGET + " INTEGER, " +
                COLUMN_JOB_IMAGE + " TEXT, " +
                COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + COLUMN_CLIENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(createJobsTable);

        String createApplicationsTable = "CREATE TABLE " + TABLE_APPLICATIONS + " (" +
                COLUMN_APP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_JOB_ID_FK + " INTEGER, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_APP_STATUS + " TEXT DEFAULT 'pending', " +
                COLUMN_APPLIED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + COLUMN_JOB_ID_FK + ") REFERENCES " + TABLE_JOBS + "(" + COLUMN_JOB_ID + "), " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(createApplicationsTable);

        // Table Notifications
        String createNotificationsTable = "CREATE TABLE notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "is_read INTEGER DEFAULT 0, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))";
        db.execSQL(createNotificationsTable);

        // Insert sample data
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_EMAIL, "admin@gmail.com");
        adminValues.put(COLUMN_USERNAME, "Admin");
        adminValues.put(COLUMN_NAME, "Administrator");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_ROLE, "Admin");
        adminValues.put(COLUMN_COMPANY, "System");
        db.insert(TABLE_USERS, null, adminValues);

        ContentValues clientValues = new ContentValues();
        clientValues.put(COLUMN_EMAIL, "client@example.com");
        clientValues.put(COLUMN_USERNAME, "johndoe");
        clientValues.put(COLUMN_NAME, "John Doe");
        clientValues.put(COLUMN_PASSWORD, "password123");
        clientValues.put(COLUMN_ROLE, "client");
        clientValues.put(COLUMN_COMPANY, "Tech Corp");
        long clientId = db.insert(TABLE_USERS, null, clientValues);

        ContentValues freelancerValues = new ContentValues();
        freelancerValues.put(COLUMN_EMAIL, "freelancer@example.com");
        freelancerValues.put(COLUMN_USERNAME, "janedoe");
        freelancerValues.put(COLUMN_NAME, "Jane Doe");
        freelancerValues.put(COLUMN_PASSWORD, "password123");
        freelancerValues.put(COLUMN_ROLE, "freelancer");
        freelancerValues.put(COLUMN_COMPANY, "Freelance");
        long freelancerId = db.insert(TABLE_USERS, null, freelancerValues);

        ContentValues jobValues1 = new ContentValues();
        jobValues1.put(COLUMN_JOB_TITLE, "Android Developer");
        jobValues1.put(COLUMN_JOB_DESCRIPTION, "Looking for experienced Android developer to build mobile app");
        jobValues1.put(COLUMN_CLIENT_ID, clientId);
        jobValues1.put(COLUMN_JOB_STATUS, "active");
        jobValues1.put(COLUMN_BUDGET, 5000000);
        jobValues1.put(COLUMN_JOB_IMAGE, "android_dev_job.jpg");
        long jobId1 = db.insert(TABLE_JOBS, null, jobValues1);

        ContentValues jobValues2 = new ContentValues();
        jobValues2.put(COLUMN_JOB_TITLE, "Web Designer");
        jobValues2.put(COLUMN_JOB_DESCRIPTION, "Need creative web designer for company website");
        jobValues2.put(COLUMN_CLIENT_ID, clientId);
        jobValues2.put(COLUMN_JOB_STATUS, "completed");
        jobValues2.put(COLUMN_BUDGET, 3000000);
        jobValues2.put(COLUMN_JOB_IMAGE, "web_design_job.jpg");
        long jobId2 = db.insert(TABLE_JOBS, null, jobValues2);

        ContentValues appValues = new ContentValues();
        appValues.put(COLUMN_JOB_ID_FK, jobId1);
        appValues.put(COLUMN_USER_ID, freelancerId);
        appValues.put(COLUMN_APP_STATUS, "pending");
        db.insert(TABLE_APPLICATIONS, null, appValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_JOBS + " ADD COLUMN " + COLUMN_JOB_IMAGE + " TEXT");
        }
    }

    public boolean registerUser(String email, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_NAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_COMPANY, "");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_EMAIL},
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ? AND " + COLUMN_ROLE + " = ?",
                new String[]{email, password, role},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean deleteUser(String email) {
        if (email.equals("admin@gmail.com")) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COLUMN_EMAIL + "=?", new String[]{email});
        return result > 0;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, null, null, null, null, COLUMN_USERNAME + " ASC");
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public String getUsernameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        String username = null;
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }
        cursor.close();
        return username;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }


    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        return userId;
    }

    public String getUserRoleByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String role = "";

        try {
            cursor = db.rawQuery("SELECT role FROM users WHERE email = ?", new String[]{email});
            if (cursor.moveToFirst()) {
                role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return role;
    }

    public boolean addJob(String title, String description, int clientId, int budget, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOB_TITLE, title);
        values.put(COLUMN_JOB_DESCRIPTION, description);
        values.put(COLUMN_CLIENT_ID, clientId);
        values.put(COLUMN_JOB_STATUS, "active");
        values.put(COLUMN_BUDGET, budget);
        values.put(COLUMN_JOB_IMAGE, imagePath);

        long result = db.insert(TABLE_JOBS, null, values);
        return result != -1;
    }

    public boolean updateJob(int jobId, String title, String description, int budget, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOB_TITLE, title);
        values.put(COLUMN_JOB_DESCRIPTION, description);
        values.put(COLUMN_BUDGET, budget);
        values.put(COLUMN_JOB_IMAGE, imagePath);

        int result = db.update(TABLE_JOBS, values, COLUMN_JOB_ID + " = ?", new String[]{String.valueOf(jobId)});
        return result > 0;
    }

    public boolean deleteJob(int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_JOBS, COLUMN_JOB_ID + " = ?", new String[]{String.valueOf(jobId)});
        return result > 0;
    }

    public boolean addJob(String title, String description, int clientId, int budget) {
        return addJob(title, description, clientId, budget, null);
    }

    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);

        try {
            int rowsAffected = db.update("applications", values, "id = ?",
                    new String[]{String.valueOf(applicationId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public boolean updateJobStatus(int jobId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOB_STATUS, status);

        int result = db.update(TABLE_JOBS, values, COLUMN_JOB_ID + " = ?",
                new String[]{String.valueOf(jobId)});
        return result > 0;
    }

    public boolean updateJobImage(int jobId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOB_IMAGE, imagePath);

        int result = db.update(TABLE_JOBS, values, COLUMN_JOB_ID + " = ?",
                new String[]{String.valueOf(jobId)});
        return result > 0;
    }

    // Notifikasi
    public long insertNotification(String userId, String title, String message, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("message", message);
        values.put("timestamp", timestamp);
        values.put("is_read", 0); // 0 untuk belum dibaca

        return db.insert("notifications", null, values);
    }

    // Mengambil notifikasi
    public List<NotificationItem> getNotificationsForUser(String userId) {
        List<NotificationItem> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("notifications",
                new String[]{"id", "title", "message", "timestamp", "is_read"},
                "user_id = ?",
                new String[]{userId},
                null, null, "timestamp DESC");

        if (cursor.moveToFirst()) {
            do {
                notifications.add(new NotificationItem(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getInt(4) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    // Method untuk mendapatkan nama user berdasarkan ID
    public int getApplicantIdForApplication(int applicationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPLICATIONS,
                new String[]{COLUMN_USER_ID},
                COLUMN_APP_ID + " = ?",
                new String[]{String.valueOf(applicationId)},
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    // Method untuk menandai notifikasi sebagai sudah dibaca
    public boolean markNotificationAsRead(String notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);

        int result = db.update("notifications", values, "id = ?",
                new String[]{notificationId});
        return result > 0;
    }

    // Method untuk menghitung notifikasi yang belum dibaca
    public int getUnreadNotificationCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0",
                new String[]{userId});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor getAllJobs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_JOBS, null, null, null, null, null, COLUMN_CREATED_AT + " DESC");
    }

    public Cursor getAllApplications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_APPLICATIONS, null, null, null, null, null, COLUMN_APPLIED_AT + " DESC");
    }

    public boolean addApplication(int jobId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOB_ID_FK, jobId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_APP_STATUS, "pending");

        long result = db.insert(TABLE_APPLICATIONS, null, values);
        return result != -1;
    }

    public Cursor getAvailableJobsForFreelancer(int freelancerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT j.*, u.name as client_name, u.company " +
                        "FROM jobs j " +
                        "JOIN users u ON j.client_id = u.id " +
                        "WHERE j.status = 'active' " +
                        "AND j.id NOT IN (SELECT job_id FROM applications WHERE user_id = ?) " +
                        "ORDER BY j.created_at DESC",
                new String[]{String.valueOf(freelancerId)});
    }

    public Cursor getFreelancerApplications(int freelancerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT a.*, j.title, j.description, j.budget, j.image_path, u.name as client_name, u.company " +
                        "FROM applications a " +
                        "JOIN jobs j ON a.job_id = j.id " +
                        "JOIN users u ON j.client_id = u.id " +
                        "WHERE a.user_id = ? " +
                        "ORDER BY a.applied_at DESC",
                new String[]{String.valueOf(freelancerId)});
    }

    public List<String> getCompaniesChattedWith(int userId) {
        List<String> companyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT u.company FROM users u " +
                "WHERE u.company IS NOT NULL";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String company = cursor.getString(0);
                if (company != null && !company.isEmpty()) {
                    companyList.add(company);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return companyList;
    }

    public Cursor searchJobsByTitle(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_JOBS,
                null,
                COLUMN_JOB_TITLE + " LIKE ?",
                new String[]{"%" + keyword + "%"},
                null, null,
                COLUMN_CREATED_AT + " DESC");
    }

    public String getJobImagePath(int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_JOBS,
                new String[]{COLUMN_JOB_IMAGE},
                COLUMN_JOB_ID + " = ?",
                new String[]{String.valueOf(jobId)},
                null, null, null);

        String imagePath = null;
        if (cursor.moveToFirst()) {
            int colIndex = cursor.getColumnIndex(COLUMN_JOB_IMAGE);
            if (colIndex != -1) {
                imagePath = cursor.getString(colIndex);
            }
        }
        cursor.close();
        return imagePath;
    }
}