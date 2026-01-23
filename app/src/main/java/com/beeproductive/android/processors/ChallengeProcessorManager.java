package com.beeproductive.android.processors;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.beeproductive.android.ServerConfig;
import com.beeproductive.android.models.Challenge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Manages challenge processing and status updates
 * Coordinates different processor types and communicates with backend
 */
public class ChallengeProcessorManager {

    private static final String TAG = "ChallengeProcessorMgr";
    private static ChallengeProcessorManager instance;

    private final Context context;
    private final Map<String, BaseChallengeProcessor> processors;
    private final OkHttpClient httpClient;
    private final FirebaseAuth mAuth;

    private ChallengeProcessorManager(Context context) {
        this.context = context.getApplicationContext();
        this.mAuth = FirebaseAuth.getInstance();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        // Initialize processors
        this.processors = new HashMap<>();
        processors.put("DAILY_LIMIT", new DailyLimitProcessor(context));
        processors.put("APP_BLOCKING", new AppBlockingProcessor(context));
        processors.put("SCREEN_TIME_REDUCTION", new ScreenTimeReductionProcessor(context));
    }

    public static synchronized ChallengeProcessorManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChallengeProcessorManager(context);
        }
        return instance;
    }

    /**
     * Process all challenges and update their status
     */
    public void processAllChallenges(List<Challenge> challenges) {
        Log.d(TAG, "Processing " + challenges.size() + " challenges");

        for (Challenge challenge : challenges) {
            processChallenge(challenge);
        }
    }

    /**
     * Process a single challenge
     */
    public void processChallenge(Challenge challenge) {
        // Skip if already completed or failed
        String currentStatus = challenge.getStatus();
        if ("COMPLETED".equals(currentStatus) || "FAILED".equals(currentStatus)) {
            Log.d(TAG, "Challenge " + challenge.getName() + " already " + currentStatus);
            return;
        }

        // Get appropriate processor
        String type = challenge.getType();
        BaseChallengeProcessor processor = processors.get(type);

        if (processor == null) {
            Log.w(TAG, "No processor found for challenge type: " + type);
            return;
        }

        // Process challenge
        String newStatus = processor.processChallenge(challenge);

        // Update status if changed
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            Log.i(TAG, "Status change detected for " + challenge.getName() +
                  ": " + currentStatus + " -> " + newStatus);
            updateChallengeStatus(challenge, newStatus);
        }
    }

    /**
     * Update challenge status on backend
     */
    private void updateChallengeStatus(Challenge challenge, String newStatus) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot update status: user not signed in");
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                Log.e(TAG, "Failed to get authentication token");
                return;
            }

            updateChallengeStatusWithToken(challenge, newStatus, token);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Authentication failed", e);
        });
    }

    private void updateChallengeStatusWithToken(Challenge challenge, String newStatus, String token) {
        try {
            // Create JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("challengeId", challenge.getId());
            jsonBody.put("status", newStatus);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            // Build PUT request
            Request request = new Request.Builder()
                    .url(ServerConfig.ENDPOINT_CHALLENGE_UPDATE)
                    .addHeader("Authorization", "Bearer " + token)
                    .put(body)
                    .build();

            // Execute request
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Failed to update challenge status", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Successfully updated challenge " + challenge.getName() +
                              " to " + newStatus);
                        // Update local challenge object with new status
                        // This preserves all challenge-specific fields (blockedApps, maxDailyMinutes, etc.)
                        challenge.setStatus(newStatus);
                    } else {
                        Log.e(TAG, "Failed to update status: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
        }
    }

    /**
     * Interface for status update callbacks
     */
    public interface StatusUpdateListener {
        void onStatusUpdated(Challenge challenge, String newStatus);
        void onUpdateError(Challenge challenge, String error);
    }
}
