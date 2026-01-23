package com.beeproductive.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

public class JoinGroupFragment extends Fragment {

    private EditText inputGroupCode;
    private OkHttpClient httpClient;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_join, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize OkHttpClient
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ServerConfig.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        inputGroupCode = root.findViewById(R.id.inputGroupCode);
        Button btnJoin = root.findViewById(R.id.btnJoinGroup);

        // Header wiring
        ImageButton backBtn = root.findViewById(R.id.backBtn);
        //TextView headerTitle = root.findViewById(R.id.headerTitle);
       // if (headerTitle != null) headerTitle.setText("Join Group");

        btnJoin.setOnClickListener(v -> {
            String code = inputGroupCode.getText().toString().trim();
            if (code.isEmpty()) {
                inputGroupCode.setError("Enter a group code");
                return;
            }

            // Make POST request to join group
            joinGroup(code);
        });

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        return root;
    }

    private void joinGroup(String groupCode) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to join a group", Toast.LENGTH_SHORT).show();
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
            joinGroupWithToken(groupCode, token);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void joinGroupWithToken(String groupCode, String token) {
        try {
            // Create JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("groupCode", groupCode);

            // Create request body with JSON media type
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            // Build the POST request with Authorization header
            Request request = new Request.Builder()
                    .url(ServerConfig.ENDPOINT_GROUPS_JOIN)
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            // Execute request asynchronously
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // Handle network failure on main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(requireContext(),
                                "Failed to join group: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
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
                                String challengeType = jsonResponse.optString("challengeType", "Unknown");

                                // Navigate to GroupFragment with the joined group details
                                Bundle args = new Bundle();
                                args.putString("groupCode", groupCode);
                                args.putString("groupName", groupName);
                                args.putString("challengeType", challengeType);

                                NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                                nav.navigate(R.id.groupFragment, args);

                                Toast.makeText(requireContext(),
                                        "Successfully joined " + groupName,
                                        Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                Toast.makeText(requireContext(),
                                        "Error parsing response",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle error response
                            String errorMsg = "Failed to join group";
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

        } catch (JSONException e) {
            Toast.makeText(requireContext(),
                    "Error creating request: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
