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

import com.beeproductive.android.adapters.ChallengesAdapter;
import com.beeproductive.android.models.Challenge;

import java.util.ArrayList;
import java.util.List;

public class MyChallengesFragment extends Fragment {
    private RecyclerView rv;
    private ChallengesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Header wiring
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        TextView headerTitle = view.findViewById(R.id.headerTitle);
        if (headerTitle != null) headerTitle.setText("My Challenges");
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                NavController nav = Navigation.findNavController(view);
                nav.navigateUp();
            });
        }

        rv = view.findViewById(R.id.rvMyChallenges);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChallengesAdapter(new ArrayList<>(), c -> openDetails(c));
        rv.setAdapter(adapter);

        // TODO: load user challenges from backend; demo for now
        List<Challenge> demo = new ArrayList<>();
        demo.add(new Challenge("1", "Morning Walk", "Easy", 4, "Walk every morning"));
        adapter.setItems(demo);
    }

    private void openDetails(Challenge c) {
        Bundle args = new Bundle();
        args.putString("name", c.getName());
        args.putString("level", c.getLevel());
        args.putInt("users", c.getUsersCount());
        args.putString("description", c.getDescription());
        NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
        nav.navigate(R.id.challengeDetailFragment, args);
    }
}
