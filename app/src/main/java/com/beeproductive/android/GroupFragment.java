package com.beeproductive.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class GroupFragment extends Fragment {

    private TextView groupTitle, groupCode, activeChallengeDesc, usersList;
    private ProgressBar groupProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);

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

            if (groupTitle != null) groupTitle.setText(name);
            groupCode.setText("Code: " + code);
            usersList.setText("You are the first member");

            if (headerTitle != null) headerTitle.setText(name);
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
