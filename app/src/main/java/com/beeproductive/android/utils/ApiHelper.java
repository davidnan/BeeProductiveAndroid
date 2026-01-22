package com.beeproductive.android.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.beeproductive.android.ServerConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utility class for making authenticated API requests with Firebase tokens
 */
public class ApiHelper {

    private static OkHttpClient httpClient;

    /**
     * Get or create the shared OkHttpClient instance
     */
    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    /**
     * Interface for handling API responses
     */
    public interface ApiCallback {
        void onSuccess(String responseBody);
        void onError(String errorMessage, int statusCode);
    }

    /**
     * Make an authenticated POST request with Firebase token
     *
     * @param context Application context for Toast messages
     * @param url API endpoint URL
     * @param jsonBody Request body as JSONObject
     * @param callback Callback for handling response
     */
    public static void makeAuthenticatedPostRequest(Context context, String url,
                                                      JSONObject jsonBody, ApiCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("Please sign in to continue", 401);
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                callback.onError("Failed to get authentication token", 401);
                return;
            }

            try {
                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(body)
                        .build();

                getHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.onError("Network error: " + e.getMessage(), -1);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseBody = response.body() != null ? response.body().string() : "";

                        if (response.isSuccessful()) {
                            callback.onSuccess(responseBody);
                        } else {
                            String errorMsg = "Request failed";
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                errorMsg = errorJson.optString("message", errorMsg);
                            } catch (JSONException e) {
                                // Use default error message
                            }
                            callback.onError(errorMsg, response.code());
                        }
                    }
                });

            } catch (Exception e) {
                callback.onError("Error creating request: " + e.getMessage(), -1);
            }

        }).addOnFailureListener(e -> {
            callback.onError("Authentication failed: " + e.getMessage(), 401);
        });
    }

    /**
     * Make an authenticated GET request with Firebase token
     *
     * @param context Application context for Toast messages
     * @param url API endpoint URL
     * @param callback Callback for handling response
     */
    public static void makeAuthenticatedGetRequest(Context context, String url, ApiCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("Please sign in to continue", 401);
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(result -> {
            String token = result.getToken();
            if (token == null) {
                callback.onError("Failed to get authentication token", 401);
                return;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();

            getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError("Network error: " + e.getMessage(), -1);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        callback.onSuccess(responseBody);
                    } else {
                        String errorMsg = "Request failed";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException e) {
                            // Use default error message
                        }
                        callback.onError(errorMsg, response.code());
                    }
                }
            });

        }).addOnFailureListener(e -> {
            callback.onError("Authentication failed: " + e.getMessage(), 401);
        });
    }
}
