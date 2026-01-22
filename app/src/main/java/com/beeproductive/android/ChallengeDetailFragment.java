package com.beeproductive.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ChallengeDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_challenge_detail, container, false);

        ImageButton backBtn = root.findViewById(R.id.backBtn);
        TextView headerTitle = root.findViewById(R.id.headerTitle);

        TextView level = root.findViewById(R.id.challengeLevel);
        TextView users = root.findViewById(R.id.challengeUsersCount);
        TextView desc = root.findViewById(R.id.challengeDescription);

        Bundle args = getArguments();
        if (args != null) {
            level.setText("Level: " + args.getString("level", "Unknown"));
            users.setText("Users: " + args.getInt("users", 0));
            desc.setText(args.getString("description", ""));
            if (headerTitle != null) headerTitle.setText(args.getString("name", "Challenge"));
        }

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        return root;
    }
}
