package com.beeproductive.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.beeproductive.android.services.ChallengeProcessingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView welcomeTextView;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate your current home page layout here
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Start challenge processing service
        startChallengeProcessingService();

        // Wire Make Honey button to navigate to All Challenges
        Button makeHoneyBtn = view.findViewById(R.id.makeHoneyBtn);
        if (makeHoneyBtn != null) {
            makeHoneyBtn.setOnClickListener(v -> {
                try {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.allChallengesFragment);
                } catch (Exception e) {
                    // Fallback: try to find nav controller from the view itself
                    try {
                        NavController nav = Navigation.findNavController(view);
                        nav.navigate(R.id.allChallengesFragment);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        return view;
    }

    private void startChallengeProcessingService() {
        Intent serviceIntent = new Intent(requireContext(), ChallengeProcessingService.class);
        requireContext().startService(serviceIntent);
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
