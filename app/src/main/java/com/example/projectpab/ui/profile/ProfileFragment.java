package com.example.projectpab.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectpab.EditProfileActivity;
import com.example.projectpab.LoginActivity;
import com.example.projectpab.R;

public class ProfileFragment extends Fragment {

    private static final int EDIT_PROFILE_REQUEST = 1;
    private static final String PREFS_NAME = "profile_data";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_HOBBY = "hobby";

    private TabHost tabHost;
    private TextView tvName, tvEmail, tvHobby;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize TextViews
        tvName = root.findViewById(R.id.tvName);
        tvEmail = root.findViewById(R.id.tvEmail);
        tvHobby = root.findViewById(R.id.tvHobby);

        // Initialize TabHost
        tabHost = root.findViewById(R.id.tabHost);
        tabHost.setup();

        // Setup tabs
        setupTabs(root);

        // Edit Profile button
        Button btnEdit = root.findViewById(R.id.btnEditProfile);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        // Logout button
        Button btnLogout = root.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Load profile data
        loadProfileData();

        return root;
    }

    private void setupTabs(View root) {
        // Tab "Tentang"
        TabHost.TabSpec spec = tabHost.newTabSpec("Tentang");
        spec.setContent(R.id.tabTentang);
        spec.setIndicator("Tentang");
        tabHost.addTab(spec);

        // Tab "Portofolio"
        spec = tabHost.newTabSpec("Portofolio");
        spec.setContent(R.id.tabPortofolio);
        spec.setIndicator("Portofolio");
        tabHost.addTab(spec);

        // Tab "Ulasan"
        spec = tabHost.newTabSpec("Ulasan");
        spec.setContent(R.id.tabUlasan);
        spec.setIndicator("Ulasan");
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

    private void loadProfileData() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load data with default values
        String name = preferences.getString(KEY_FULL_NAME, "Muhamad Hamdan Alfarizi");
        String email = preferences.getString(KEY_EMAIL, "hamdanalfarizi407@gmail.com");
        String hobby = preferences.getString(KEY_HOBBY, "Playing Music and Volly Ball");

        // Set text to TextViews
        tvName.setText(name);
        tvEmail.setText(email);
        tvHobby.setText(hobby);

        // Log loaded data for debugging
        Log.d("ProfileData", "Loaded: " + name + ", " + email + ", " + hobby);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == getActivity().RESULT_OK) {
            loadProfileData(); // Refresh data after edit
            Log.d("ProfileData", "Profile updated - refreshing data");
        }
    }

    private void showLogoutConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear preferences
                    SharedPreferences sharedPref = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    sharedPref.edit().clear().apply();

                    // Go to login activity
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}