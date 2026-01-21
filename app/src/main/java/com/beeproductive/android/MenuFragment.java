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

        return root;
    }
}
