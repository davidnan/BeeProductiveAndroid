package com.beeproductive.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView welcomeTextView;
    private FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate your current home page layout here
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
//         Any additional code to run when the fragment resumes
        welcomeTextView = getView().findViewById(R.id.welcomeText);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            // You can use the token as needed
            }).addOnFailureListener(e -> {
            // Handle error
        });
        if (currentUser == null) {
            welcomeTextView.setText("Welcome, Guest!");
        } else {
            String displayName = currentUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = "User";
            }
            welcomeTextView.setText("Welcome, " + displayName + "!");
        }


    }
}
