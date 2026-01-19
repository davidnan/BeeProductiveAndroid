package com.beeproductive.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        NavController navController = Navigation.findNavController(this, R.id.navHost);

        // Let NavigationUI handle all fragment menu items automatically
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Handle MenuActivity separately
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu) {
                startActivity(new Intent(this, MenuActivity.class));
                return true;
            } else {
                // Let NavController handle fragments
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });


        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            welcomeText.setText("Welcome, " + (displayName != null ? displayName : email) + "!");
        } else {
            // If no user is signed in, redirect to MainActivity
            navigateToMain();
            return;
        }
    }

    private void signOut() {
        mAuth.signOut();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
