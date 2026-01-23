package com.beeproductive.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.beeproductive.android.models.LeaderboardResponse;
import com.beeproductive.android.models.LeaderboardUserDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupFragment extends Fragment {

    private TextView groupTitle, groupCode, activeChallengeDesc, usersList;
    private ProgressBar groupProgressBar;
    private FirebaseAuth mAuth;
    private OkHttpClient httpClient;
    private String currentGroupCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize OkHttpClient
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        // Header wiring
        ImageButton backBtn = root.findViewById(R.id.backBtn);
        TextView headerTitle = root.findViewById(R.id.headerTitle);

        groupCode = root.findViewById(R.id.groupCode);
        usersList = root.findViewById(R.id.usersList);

        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("groupName", "Group Name");
            String code = args.getString("groupCode", "XXXXX");
            String challengeType = args.getString("challengeType", "") + "";

            currentGroupCode = code;

            if (groupTitle != null) groupTitle.setText(name);
            groupCode.setText("Code: " + code);
            usersList.setText("Loading...");

            if (headerTitle != null) headerTitle.setText(name);

            // Fetch leaderboard data
            fetchLeaderboard(code);
        }

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        return root;
    }

    private void fetchLeaderboard(String groupCode) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view leaderboard", Toast.LENGTH_SHORT).show();
            usersList.setText("Sign in required");
            return;
        }

        // Get Firebase ID token
        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                Toast.makeText(requireContext(), "Failed to get authentication token", Toast.LENGTH_SHORT).show();
                usersList.setText("Authentication failed");
                return;
            }

            // Now make the request with the token
            fetchLeaderboardWithToken(groupCode, token);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            usersList.setText("Authentication failed");
        });
    }

    private void fetchLeaderboardWithToken(String groupCode, String token) {
        // Build the GET request with Authorization header
        String url = ServerConfig.ENDPOINT_LEADERBOARD + "/" + groupCode;
        Request request = new Request.Builder()
                .url(url)
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
                            "Failed to load leaderboard: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    usersList.setText("Failed to load leaderboard");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Parse response
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String groupName = jsonResponse.optString("groupName", "Unknown Group");
                            String code = jsonResponse.optString("groupCode", currentGroupCode);
                            JSONArray leaderboardArray = jsonResponse.optJSONArray("leaderboard");

                            // Display leaderboard
                            displayLeaderboard(leaderboardArray);

                        } catch (JSONException e) {
                            Toast.makeText(requireContext(),
                                    "Error parsing leaderboard data",
                                    Toast.LENGTH_SHORT).show();
                            usersList.setText("Error parsing data");
                        }
                    } else {
                        // Handle error response
                        String errorMsg = "Failed to load leaderboard";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException e) {
                            // Use default error message
                        }
                        Toast.makeText(requireContext(),
                                errorMsg + " (Code: " + response.code() + ")",
                                Toast.LENGTH_LONG).show();
                        usersList.setText("Failed to load leaderboard");
                    }
                });
            }
        });
    }

    private void displayLeaderboard(JSONArray leaderboardArray) {
        if (leaderboardArray == null || leaderboardArray.length() == 0) {
            usersList.setText("No members yet");
            return;
        }

        StringBuilder leaderboardText = new StringBuilder();
        try {
            for (int i = 0; i < leaderboardArray.length(); i++) {
                JSONObject userObj = leaderboardArray.getJSONObject(i);
                int rank = userObj.optInt("rank", i + 1);
                String name = userObj.optString("name", "Unknown");
                int numberOfBees = userObj.optInt("numberOfBees", 0);

                // Format: "1. John Doe - 150 🐝"
                leaderboardText.append(rank)
                        .append(". ")
                        .append(name)
                        .append(" - ")
                        .append(numberOfBees)
                        .append(" 🐝");

                if (i < leaderboardArray.length() - 1) {
                    leaderboardText.append("\n");
                }
            }

            usersList.setText(leaderboardText.toString());

        } catch (JSONException e) {
            usersList.setText("Error displaying leaderboard");
            Toast.makeText(requireContext(), "Error parsing leaderboard data", Toast.LENGTH_SHORT).show();
        }
    }
}
