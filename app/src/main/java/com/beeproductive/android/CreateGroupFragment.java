package com.beeproductive.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;
import java.util.UUID;

public class CreateGroupFragment extends Fragment {

    private EditText inputGroupName;
    private Spinner spinnerChallengeType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_create, container, false);

        inputGroupName = root.findViewById(R.id.inputGroupName);
        Button btnCreate = root.findViewById(R.id.btnCreateGroup);

        // Header wiring (shared include)
        ImageButton backBtn = root.findViewById(R.id.backBtn);
        //TextView headerTitle = root.findViewById(R.id.headerTitle);
        //if (headerTitle != null) headerTitle.setText("Create Group");



        btnCreate.setOnClickListener(v -> {
            String groupName = inputGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                inputGroupName.setError("Please enter a group name");
                return;
            }

            // Generate a simple group code
            String code = generateGroupCode();

            // Pass data to GroupFragment via bundle
            Bundle args = new Bundle();
            args.putString("groupName", groupName);
            args.putString("groupCode", code);

            NavController nav = Navigation.findNavController(root);
            nav.navigate(R.id.groupFragment, args);
        });

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        return root;
    }

    private String generateGroupCode() {
        // Simple readable code: first 6 chars of UUID uppercase
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
