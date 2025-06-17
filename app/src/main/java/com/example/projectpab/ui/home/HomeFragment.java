package com.example.projectpab.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpab.NotifFreelanceActivity;
import com.example.projectpab.DatabaseHelper;
import com.example.projectpab.JobDetailActivity;
import com.example.projectpab.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerJobs;
    private JobsAdapter jobsAdapter;
    private ImageView ivNotification;

    // Database
    private DatabaseHelper dbHelper;

    // User session
    private int currentUserId;
    private String currentUserEmail;
    private String currentUserRole;

    // Data
    private List<Job> jobsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        initViews(view);

        // Get user session
        getUserSession();

        // Initialize database
        initDatabase();

        // Load jobs
        loadJobs();

        // Setup listeners
        setupListeners();

        // Tambahkan ini:
        checkUnreadNotifications();

        return view;
    }

    private void initViews(View view) {
        recyclerJobs = view.findViewById(R.id.recyclerJobs);
        ivNotification = view.findViewById(R.id.iv_notification);

        // Initialize job list and adapter
        jobsList = new ArrayList<>();
        jobsAdapter = new JobsAdapter(getContext(), jobsList);

        // Setup RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerJobs.setLayoutManager(gridLayoutManager);

        // Set spacing antar item (12dp)
        int spacingInPixels = (int) (12 * getResources().getDisplayMetrics().density);
        recyclerJobs.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        // Set adapter
        recyclerJobs.setAdapter(jobsAdapter);
    }

    private void checkUnreadNotifications() {
        if (getActivity() == null || dbHelper == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPreferences.getString("email", "");
        int currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

        if (currentUserId != -1) {
            int unreadCount = dbHelper.getUnreadNotificationCount(String.valueOf(currentUserId));
            updateNotificationBadge(unreadCount);
        }
    }

    private void updateNotificationBadge(int unreadCount) {
        if (getView() == null) return;

        TextView tvBadge = getView().findViewById(R.id.tv_notification_badge);

        if (unreadCount > 0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
        } else {
            tvBadge.setVisibility(View.GONE);
        }
    }

    private void getUserSession() {
        if (getActivity() == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");
        currentUserRole = sharedPreferences.getString("role", "");

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(getContext(), "Session expired, please login again", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void initDatabase() {
        if (getContext() == null) return;

        dbHelper = new DatabaseHelper(getContext());
        currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);
    }

    private void loadJobs() {
        if (dbHelper == null) return;

        jobsList.clear();

        Cursor cursor = null;
        try {
            // Check user role to determine which jobs to show
            if ("freelancer".equals(currentUserRole)) {
                // For freelancers, show jobs they haven't applied to yet
                cursor = dbHelper.getAvailableJobsForFreelancer(currentUserId);
            } else {
                // For other users (clients/admin), show all active jobs
                cursor = dbHelper.getAllJobs();
            }

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Check if this is an active job
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    if (!"active".equals(status)) {
                        continue;
                    }

                    Job job = new Job();
                    job.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    job.title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    job.description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    job.requirements = cursor.getString(cursor.getColumnIndexOrThrow("requirements"));
                    job.budget = cursor.getInt(cursor.getColumnIndexOrThrow("budget"));
                    job.status = status;
                    job.createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                    job.clientId = cursor.getInt(cursor.getColumnIndexOrThrow("client_id"));

                    // Get client info
                    if (cursor.getColumnIndex("client_name") != -1) {
                        job.clientName = cursor.getString(cursor.getColumnIndexOrThrow("client_name"));
                    } else {
                        job.clientName = getClientNameById(job.clientId);
                    }

                    if (cursor.getColumnIndex("company") != -1) {
                        job.company = cursor.getString(cursor.getColumnIndexOrThrow("company"));
                    } else {
                        job.company = getClientCompanyById(job.clientId);
                    }

                    jobsList.add(job);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Update adapter
        if (jobsAdapter != null) {
            jobsAdapter.notifyDataSetChanged();
        }
    }

    private String getClientNameById(int clientId) {
        if (dbHelper == null) return "Unknown";

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllUsers();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    if (id == clientId) {
                        return cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "Unknown";
    }

    private String getClientCompanyById(int clientId) {
        if (dbHelper == null) return "";

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllUsers();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    if (id == clientId) {
                        String company = cursor.getString(cursor.getColumnIndexOrThrow("company"));
                        return company != null ? company : "";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    private void setupListeners() {
        // RecyclerView item click akan dihandle di adapter
        // Tidak perlu setup listener di sini untuk RecyclerView

        //listener untuk notifikasi
        if (ivNotification != null) {
            ivNotification.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NotifFreelanceActivity.class);
                startActivity(intent);

                // Reset badge ketika notifikasi diklik
                updateNotificationBadge(0);
            });
        }
    }

    private void openJobDetail(Job job) {
        try {
            // Create intent to job detail activity
            Intent intent = new Intent(getActivity(), JobDetailActivity.class);
            intent.putExtra("job_id", job.id);
            intent.putExtra("job_title", job.title);
            intent.putExtra("job_description", job.description);
            intent.putExtra("job_requirements", job.requirements);
            intent.putExtra("job_budget", job.budget);
            intent.putExtra("client_name", job.clientName);
            intent.putExtra("company", job.company);
            intent.putExtra("client_id", job.clientId);
            intent.putExtra("current_user_id", currentUserId);
            intent.putExtra("current_user_role", currentUserRole);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening job details", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadJobs();
        checkUnreadNotifications();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // DatabaseHelper will handle database closing
    }

    // Job data class
    public static class Job {
        public int id;
        public String title;
        public String description;
        public String requirements;
        public int budget;
        public String clientName;
        public String company;
        public String status;
        public String createdAt;
        public int clientId;
    }

    // Custom adapter untuk RecyclerView
    private class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.JobViewHolder> {
        private Context context;
        private List<Job> jobs;
        private LayoutInflater inflater;

        public JobsAdapter(Context context, List<Job> jobs) {
            this.context = context;
            this.jobs = jobs;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_job_grid, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
            if (jobs != null && position < jobs.size()) {
                Job job = jobs.get(position);

                holder.tvJobTitle.setText(job.title != null ? job.title : "No Title");
                holder.tvClientName.setText(job.clientName != null ? job.clientName : "Unknown Client");
                holder.tvBudget.setText("Rp " + String.format("%,d", job.budget));
                holder.tvCompany.setText(job.company != null && !job.company.isEmpty() ? job.company : "");

                // Handle item click
                holder.itemView.setOnClickListener(v -> {
                    openJobDetail(job);
                });
            }
        }

        @Override
        public int getItemCount() {
            return jobs != null ? jobs.size() : 0;
        }

        public class JobViewHolder extends RecyclerView.ViewHolder {
            TextView tvJobTitle;
            TextView tvClientName;
            TextView tvBudget;
            TextView tvCompany;

            public JobViewHolder(@NonNull View itemView) {
                super(itemView);
                tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
                tvClientName = itemView.findViewById(R.id.tvClientName);
                tvBudget = itemView.findViewById(R.id.tvBudget);
                tvCompany = itemView.findViewById(R.id.tvCompany);
            }
        }
    }

    // Class untuk mengatur spacing antar item dalam grid
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}