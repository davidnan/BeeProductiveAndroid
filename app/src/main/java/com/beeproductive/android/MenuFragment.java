package com.beeproductive.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class MenuFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        // Find the profile button using runtime id lookup to avoid static layout checks
        int profileBtnId = getResources().getIdentifier("profileInfoBtn", "id", requireContext().getPackageName());
        if (profileBtnId != 0) {
            View profileBtn = root.findViewById(profileBtnId);
            if (profileBtn != null) {
                profileBtn.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.profileFragment);
                });
            }
        }

        // Wire the create group button
        int createBtnId = getResources().getIdentifier("createGroupBtn", "id", requireContext().getPackageName());
        if (createBtnId != 0) {
            View createBtn = root.findViewById(createBtnId);
            if (createBtn != null) {
                createBtn.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.createGroupFragment);
                });
            }
        }

        // Wire the create group text (also acts as trigger)
        int createTextId = getResources().getIdentifier("createGroupText", "id", requireContext().getPackageName());
        if (createTextId != 0) {
            View createText = root.findViewById(createTextId);
            if (createText != null) {
                createText.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.createGroupFragment);
                });
            }
        }

        // Wire the join group text
        int joinTextId = getResources().getIdentifier("joinGroupText", "id", requireContext().getPackageName());
        if (joinTextId != 0) {
            View joinText = root.findViewById(joinTextId);
            if (joinText != null) {
                joinText.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.joinGroupFragment);
                });
            }
        }

        // Wire the My Groups text
        int myGroupsTextId = getResources().getIdentifier("myGroupsText", "id", requireContext().getPackageName());
        if (myGroupsTextId != 0) {
            View myGroupsText = root.findViewById(myGroupsTextId);
            if (myGroupsText != null) {
                myGroupsText.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.myGroupsFragment);
                });
            }
        }

        // Wire My Active Challenges and All Challenges to new fragments
        int myChallengesId = getResources().getIdentifier("myChallengesText", "id", requireContext().getPackageName());
        if (myChallengesId != 0) {
            View myChallenges = root.findViewById(myChallengesId);
            if (myChallenges != null) {
                myChallenges.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.myChallengesFragment);
                });
            }
        }

        int allChallengesId = getResources().getIdentifier("allChallengesText", "id", requireContext().getPackageName());
        if (allChallengesId != 0) {
            View allChallenges = root.findViewById(allChallengesId);
            if (allChallenges != null) {
                allChallenges.setOnClickListener(v -> {
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHost);
                    nav.navigate(R.id.allChallengesFragment);
                });
            }
        }

        return root;
    }
}
