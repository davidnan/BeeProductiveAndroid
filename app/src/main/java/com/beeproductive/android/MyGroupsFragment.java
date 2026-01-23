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

public class MyGroupsFragment extends Fragment {

    private RecyclerView recyclerGroups;
    private FirebaseAuth mAuth;
    private OkHttpClient httpClient;
    private GroupAdapter adapter;
    private List<GroupItem> groupsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_groups, container, false);

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
        if (headerTitle != null) headerTitle.setText("My Groups");
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        recyclerGroups = root.findViewById(R.id.recyclerGroups);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize empty list and adapter
        groupsList = new ArrayList<>();
        adapter = new GroupAdapter(groupsList, item -> {
            // Navigate to GroupFragment with passed data
            Bundle args = new Bundle();
            args.putString("groupName", item.name);
            args.putString("groupCode", item.code);
            args.putString("challengeType", "Unknown");

            NavController nav = Navigation.findNavController(root);
            nav.navigate(R.id.groupFragment, args);
        });

        recyclerGroups.setAdapter(adapter);

        // Fetch groups from server
        fetchMyGroups();

        return root;
    }

    private void fetchMyGroups() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view your groups", Toast.LENGTH_SHORT).show();
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
            fetchMyGroupsWithToken(token);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void fetchMyGroupsWithToken(String token) {
        // Build the GET request with Authorization header
        Request request = new Request.Builder()
                .url(ServerConfig.ENDPOINT_MY_GROUPS)
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
                            "Failed to load groups: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Parse response - expecting an array of group objects
                            JSONArray groupsArray = new JSONArray(responseBody);

                            // Clear existing list
                            groupsList.clear();

                            // Parse each group
                            for (int i = 0; i < groupsArray.length(); i++) {
                                JSONObject groupObj = groupsArray.getJSONObject(i);
                                String name = groupObj.optString("name", "Unknown Group");
                                String code = groupObj.optString("code", "N/A");
                                int memberCount = groupObj.optInt("memberCount", 0);

                                groupsList.add(new GroupItem(name, code, memberCount));
                            }

                            // Notify adapter
                            adapter.notifyDataSetChanged();

                            if (groupsList.isEmpty()) {
                                Toast.makeText(requireContext(),
                                        "You haven't joined any groups yet",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(requireContext(),
                                    "Error parsing groups data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error response
                        String errorMsg = "Failed to load groups";
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
}
