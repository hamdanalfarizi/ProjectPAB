package com.example.projectpab.ui.search;

import android.view.MotionEvent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.example.projectpab.DatabaseHelper;
import com.example.projectpab.Job;
import com.example.projectpab.JobAdapter;
import com.example.projectpab.R;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private DatabaseHelper dbHelper;
    private List<Job> allJobs = new ArrayList<>(); // Cache semua job awal

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput = view.findViewById(R.id.search_input);
        recyclerView = view.findViewById(R.id.search_result_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());

        // Inisialisasi adapter tanpa perlu set listener manual
        // Karena click handler sudah ada di dalam JobAdapter
        jobAdapter = new JobAdapter(new ArrayList<>());
        recyclerView.setAdapter(jobAdapter);

        // Load semua data awal (bisa di async)
        loadAllJobs();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterJobs(s.toString());
            }
        });

        // Handle clear button di EditText
        searchInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (searchInput.getRight() - searchInput.getCompoundDrawables()[2].getBounds().width())) {
                    searchInput.setText("");
                    return true;
                }
            }
            return false;
        });

        return view;
    }

    private void loadAllJobs() {
        new Thread(() -> {
            Cursor cursor = dbHelper.getAllJobs();
            allJobs.clear();

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            Job job = new Job();
                            job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                            job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                            job.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            job.setBudget(cursor.getInt(cursor.getColumnIndexOrThrow("budget")));
                            job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                            job.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                            allJobs.add(job);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    cursor.close();
                }
            }

            requireActivity().runOnUiThread(() -> jobAdapter.setJobs(allJobs));
        }).start();
    }

    private void filterJobs(String query) {
        List<Job> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allJobs);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Job job : allJobs) {
                if (job.getTitle().toLowerCase().contains(lowerQuery) ||
                        (job.getDescription() != null && job.getDescription().toLowerCase().contains(lowerQuery))) {
                    filteredList.add(job);
                }
            }
        }

        jobAdapter.setJobs(filteredList);
    }
}