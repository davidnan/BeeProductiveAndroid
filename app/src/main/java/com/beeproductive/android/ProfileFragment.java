package com.beeproductive.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.beeproductive.android.utils.UsageStatsPermissionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
    private Button btnRequestPermission;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView nameTv = root.findViewById(R.id.profileName);
        TextView emailTv = root.findViewById(R.id.profileEmail);
        TextView beesTv = root.findViewById(R.id.beesCount);
        TextView honeyTv = root.findViewById(R.id.honeyCount);
        TextView streakTv = root.findViewById(R.id.streakCount);
        TextView groupsTv = root.findViewById(R.id.groupsCount);
        TextView logoutBtn = root.findViewById(R.id.logoutBtn);
        btnRequestPermission = root.findViewById(R.id.btnRequestPermission);

        if (user != null) {
            String name = user.getDisplayName() == null ? "" : user.getDisplayName();
            String email = user.getEmail() == null ? "" : user.getEmail();
            nameTv.setText(name);
            emailTv.setText(email);
        } else {
            nameTv.setText("(not signed in)");
            emailTv.setText("");
        }

        // Placeholders for app-specific stats -- adapt to your data model or load from Firestore/DB
        beesTv.setText(beesTv.getText() + " " + 0);
        honeyTv.setText(honeyTv.getText() + " " + 0);
        streakTv.setText(streakTv.getText() + " " + 0);
        groupsTv.setText(groupsTv.getText() + " " + 0);

        // Setup permission button
        checkUsageStatsPermission();

        logoutBtn.setOnClickListener(v -> {
            try {
                mAuth.signOut();
                Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to sign out", e);
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show();
            }
        });


        return root;
    }

    private void checkUsageStatsPermission() {
        if (!UsageStatsPermissionHelper.hasUsageStatsPermission(requireContext())) {
            // Show permission button
            btnRequestPermission.setVisibility(View.VISIBLE);

            // Set click listener to open settings
            btnRequestPermission.setOnClickListener(v -> {
                Intent intent = UsageStatsPermissionHelper.getUsageStatsSettingsIntent();
                startActivity(intent);
                Toast.makeText(requireContext(),
                        "Please enable Usage Access for BeeProductive",
                        Toast.LENGTH_LONG).show();
            });
        } else {
            // Hide button if permission already granted
            btnRequestPermission.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recheck permission when user returns from settings
        if (btnRequestPermission != null) {
            if (UsageStatsPermissionHelper.hasUsageStatsPermission(requireContext())) {
                btnRequestPermission.setVisibility(View.GONE);
            } else {
                btnRequestPermission.setVisibility(View.VISIBLE);
            }
        }
    }
}
