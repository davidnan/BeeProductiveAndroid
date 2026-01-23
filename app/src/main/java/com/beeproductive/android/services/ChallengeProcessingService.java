package com.beeproductive.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beeproductive.android.ServerConfig;
import com.beeproductive.android.models.Challenge;
import com.beeproductive.android.processors.ChallengeProcessorManager;
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

/**
 * Background service that periodically processes challenges
 * Checks usage stats and updates challenge status
 */
public class ChallengeProcessingService extends Service {

    private static final String TAG = "ChallengeProcessingSvc";
    private static final long PROCESSING_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

    private Handler handler;
    private Runnable processingRunnable;
    private OkHttpClient httpClient;
    private FirebaseAuth mAuth;
    private ChallengeProcessorManager processorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        mAuth = FirebaseAuth.getInstance();
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
        processorManager = ChallengeProcessorManager.getInstance(this);

        handler = new Handler(Looper.getMainLooper());
        processingRunnable = new Runnable() {
            @Override
            public void run() {
                processChallenges();
                // Schedule next processing
                handler.postDelayed(this, PROCESSING_INTERVAL_MS);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Start periodic processing
        handler.post(processingRunnable);

        return START_STICKY;
    }

    private void processChallenges() {
        Log.d(TAG, "Starting challenge processing cycle");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user signed in, skipping processing");
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token != null) {
                fetchAndProcessChallenges(token);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get auth token", e);
        });
    }

    private void fetchAndProcessChallenges(String token) {
        Request request = new Request.Builder()
                .url(ServerConfig.ENDPOINT_MY_CHALLENGES)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch challenges", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response.code());
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";

                try {
                    JSONArray challengesArray = new JSONArray(responseBody);
                    List<Challenge> challenges = parseChallenges(challengesArray);

                    Log.d(TAG, "Processing " + challenges.size() + " challenges");
                    processorManager.processAllChallenges(challenges);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing challenges", e);
                }
            }
        });
    }

    private List<Challenge> parseChallenges(JSONArray challengesArray) throws JSONException {
        List<Challenge> challenges = new ArrayList<>();

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

            Challenge challenge = new Challenge(id, name, description, type,
                    startDate, endDate, rewardBees,
                    reductionPercentage,
                    blockedApps.isEmpty() ? null : blockedApps,
                    maxDailyMinutes,
                    true);
            challenge.setStatus(status);
            challenges.add(challenge);
        }

        return challenges;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        // Stop processing
        if (handler != null && processingRunnable != null) {
            handler.removeCallbacks(processingRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
