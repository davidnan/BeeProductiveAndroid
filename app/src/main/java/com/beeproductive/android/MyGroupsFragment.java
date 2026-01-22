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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsFragment extends Fragment {

    private RecyclerView recyclerGroups;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_groups, container, false);

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

        List<GroupItem> demo = new ArrayList<>();
        demo.add(new GroupItem("Bees United", "ABC123", 5));
        demo.add(new GroupItem("Honey Lovers", "XYZ789", 3));
        demo.add(new GroupItem("Early Risers", "ER4567", 8));

        GroupAdapter adapter = new GroupAdapter(demo, item -> {
            // Navigate to GroupFragment with passed data
            Bundle args = new Bundle();
            args.putString("groupName", item.name);
            args.putString("groupCode", item.code);
            args.putString("challengeType", "Unknown");

            NavController nav = Navigation.findNavController(root);
            nav.navigate(R.id.groupFragment, args);
        });

        recyclerGroups.setAdapter(adapter);

        return root;
    }
}
