package com.beeproductive.android;

import android.os.Bundle;
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

public class JoinGroupFragment extends Fragment {

    private EditText inputGroupCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_join, container, false);

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

            // For now, just navigate to GroupFragment and pass the code; real lookup can be added later
            Bundle args = new Bundle();
            args.putString("groupCode", code);
            args.putString("groupName", "Joined Group");
            args.putString("challengeType", "Unknown");

            NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
            nav.navigate(R.id.groupFragment, args);

            Toast.makeText(requireContext(), "Joined group " + code, Toast.LENGTH_SHORT).show();
        });

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(root);
                nav.navigateUp();
            });
        }

        return root;
    }
}
