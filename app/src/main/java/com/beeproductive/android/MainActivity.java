package com.beeproductive.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1001; // kept for logging/tracking

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
            return;
        }

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configure Google Sign-In
        String clientId = getString(R.string.default_web_client_id);
        if (clientId == null || clientId.trim().isEmpty()) {
            Log.e(TAG, "default_web_client_id is missing or empty");
            Toast.makeText(this, "Google client ID missing - check strings.xml / google-services.json", Toast.LENGTH_LONG).show();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Activity Result API launcher (replaces deprecated startActivityForResult)
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        Log.d(TAG, "onActivityResult (ActivityResult API) returned, data=" + (data != null));
                        // Mirror previous onActivityResult behavior for RC_SIGN_IN
                        if (data == null) {
                            Log.w(TAG, "Google sign-in returned null intent data (user likely cancelled or an error occurred)");
                            Toast.makeText(MainActivity.this, "Google sign-in cancelled or failed to return data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                // Authenticate with Firebase
                                firebaseAuthWithGoogle(account.getIdToken());
                                // Save to SharedPreferences for future auto-login
                                saveCredential(account.getEmail());
                            } else {
                                Log.w(TAG, "GoogleSignInAccount was null after sign-in");
                                Toast.makeText(MainActivity.this, "Google sign-in failed: no account returned", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign-in failed", e);
                            String msg = e.getMessage() == null ? "Unknown API exception" : e.getMessage();
                            Toast.makeText(MainActivity.this, "Google sign-in failed: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set up Google Sign-In button
        Button googleSignInButton = findViewById(R.id.googleSignInBtn);
        googleSignInButton.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In button clicked");
            signInWithGoogle();
        });
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Log.e(TAG, "googleSignInClient is null - cannot start sign-in");
            Toast.makeText(this, "Sign-in not available right now", Toast.LENGTH_SHORT).show();
            return;
        }

        int availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (availability != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services not available: " + availability);
            Toast.makeText(this, "Google Play Services not available or outdated", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            if (signInIntent == null) {
                Log.e(TAG, "googleSignInClient.getSignInIntent() returned null");
                Toast.makeText(this, "Unable to create sign-in intent", Toast.LENGTH_LONG).show();
                return;
            }
            if (signInLauncher == null) {
                Log.e(TAG, "signInLauncher is null - registration may have failed");
                Toast.makeText(this, "Sign-in launcher not initialized", Toast.LENGTH_LONG).show();
                return;
            }

            signInLauncher.launch(signInIntent);
            Log.d(TAG, "Launched Google sign-in intent");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Google sign-in intent", e);
            Toast.makeText(this, "Failed to start Google sign-in: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Keep legacy onActivityResult to avoid surprises from other flows; it will defer to the new launcher if used
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // no-op: we handle sign-in with the Activity Result API above
        Log.d(TAG, "onActivityResult called (legacy), requestCode=" + requestCode + ", resultCode=" + resultCode);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Log.e(TAG, "Received null idToken from GoogleSignInAccount");
            Toast.makeText(this, "Authentication failed: missing token", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Welcome " + user.getDisplayName(),
                                    Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        }
                    } else {
                        Log.w(TAG, "Firebase authentication failed", task.getException());
                        Exception ex = task.getException();
                        String msg = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "Unknown error";
                        Toast.makeText(this, "Authentication failed: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCredential(String email) {
        if (email == null) return;

        // Save the email locally as a simple fallback (used for app-side auto-login hints)
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("saved_email", email)
                .apply();

        Log.d(TAG, "Credential (email) saved locally");
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
