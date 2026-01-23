package com.beeproductive.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beeproductive.android.adapters.ChallengesAdapter;
import com.beeproductive.android.models.Challenge;
import com.beeproductive.android.processors.ChallengeProcessorManager;
import com.beeproductive.android.utils.UsageStatsPermissionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyChallengesFragment extends Fragment {
    private RecyclerView rv;
    private ChallengesAdapter adapter;
    private FirebaseAuth mAuth;
    private OkHttpClient httpClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize OkHttpClient
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        // Header wiring
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        TextView headerTitle = view.findViewById(R.id.headerTitle);
        if (headerTitle != null) headerTitle.setText("My Challenges");
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(view);
                nav.navigateUp();
            });
        }

        rv = view.findViewById(R.id.rvMyChallenges);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChallengesAdapter(new ArrayList<>(), new ChallengesAdapter.Listener() {
            @Override
            public void onDetailsClicked(Challenge c) {
                openDetails(c);
            }

            @Override
            public void onJoinClicked(Challenge c) {
                // My challenges are already joined, so this shouldn't be called
            }
        });
        rv.setAdapter(adapter);

        // Fetch challenges from backend
        fetchMyChallenges();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-fetch and process challenges when user returns to this tab
        // This ensures challenges are always up-to-date with latest status
        fetchMyChallenges();
    }

    private void fetchMyChallenges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view your challenges", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Firebase ID token
        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                Toast.makeText(requireContext(), "Failed to get authentication token", Toast.LENGTH_SHORT).show();
                return;
            }

            // Now make the request with the token
            fetchMyChallengesWithToken(token);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void fetchMyChallengesWithToken(String token) {
        // Build the GET request with Authorization header
        Request request = new Request.Builder()
                .url(ServerConfig.ENDPOINT_MY_CHALLENGES)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        // Execute request asynchronously
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle network failure on main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(),
                            "Failed to load challenges: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Log the response for debugging
                            android.util.Log.d("MyChallenges", "Response: " + responseBody);

                            // Parse response - expecting an array of challenge objects
                            JSONArray challengesArray = new JSONArray(responseBody);

                            List<Challenge> challenges = new ArrayList<>();

                            // Parse each challenge
                            for (int i = 0; i < challengesArray.length(); i++) {
                                JSONObject challengeObj = challengesArray.getJSONObject(i);

                                String id = String.valueOf(challengeObj.optInt("challengeId", 0));
                                String name = challengeObj.optString("challengeName", "Unknown Challenge");
                                String description = challengeObj.optString("challengeDescription", "");
                                String type = challengeObj.optString("challengeType", "");
                                String startDate = challengeObj.optString("startDate", "");
                                String endDate = challengeObj.optString("endDate", "");
                                int rewardBees = challengeObj.optInt("rewardBees", 0);
                                String status = challengeObj.optString("status", "ENROLLED");

                                // Parse type-specific fields
                                Integer reductionPercentage = challengeObj.isNull("reductionPercentage") ?
                                        null : challengeObj.optInt("reductionPercentage");
                                Integer maxDailyMinutes = challengeObj.isNull("maxDailyMinutes") ?
                                        null : challengeObj.optInt("maxDailyMinutes");

                                List<String> blockedApps = new ArrayList<>();
                                if (challengeObj.has("blockedApps") && !challengeObj.isNull("blockedApps")) {
                                    JSONArray appsArray = challengeObj.getJSONArray("blockedApps");
                                    for (int j = 0; j < appsArray.length(); j++) {
                                        blockedApps.add(appsArray.getString(j));
                                    }
                                }

                                // Log parsed values for debugging
                                android.util.Log.d("MyChallenges", "Challenge: " + name +
                                    ", Type: " + type +
                                    ", Reduction: " + reductionPercentage +
                                    ", MaxMinutes: " + maxDailyMinutes +
                                    ", BlockedApps: " + (blockedApps.isEmpty() ? "none" : blockedApps.size()));

                                Challenge challenge = new Challenge(id, name, description, type,
                                        startDate, endDate, rewardBees,
                                        reductionPercentage,
                                        blockedApps.isEmpty() ? null : blockedApps,
                                        maxDailyMinutes,
                                        true); // isEnrolled always true for my challenges
                                challenge.setStatus(status);
                                challenges.add(challenge);
                            }

                            // Update adapter
                            adapter.setItems(challenges);

                            // Process challenges to check status
                            if (UsageStatsPermissionHelper.hasUsageStatsPermission(requireContext())) {
                                ChallengeProcessorManager.getInstance(requireContext())
                                        .processAllChallenges(challenges);
                            }

                            if (challenges.isEmpty()) {
                                Toast.makeText(requireContext(),
                                        "You haven't joined any challenges yet",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(requireContext(),
                                    "Error parsing challenges data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error response
                        String errorMsg = "Failed to load challenges";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException e) {
                            // Use default error message
                        }
                        Toast.makeText(requireContext(),
                                errorMsg + " (Code: " + response.code() + ")",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void openDetails(Challenge c) {
        Bundle args = new Bundle();
        args.putString("challengeId", c.getId());
        args.putString("name", c.getName());
        args.putString("level", c.getLevel());
        args.putInt("users", c.getUsersCount());
        args.putString("description", c.getDescription());
        args.putString("type", c.getType());
        args.putString("startDate", c.getStartDate());
        args.putString("endDate", c.getEndDate());
        args.putInt("rewardBees", c.getRewardBees());
        NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
        nav.navigate(R.id.challengeDetailFragment, args);
    }
}
