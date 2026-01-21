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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;

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
}
