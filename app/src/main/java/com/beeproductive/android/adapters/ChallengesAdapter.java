package com.beeproductive.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beeproductive.android.R;
import com.beeproductive.android.models.Challenge;

import java.util.List;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.VH> {
    public interface Listener { void onDetailsClicked(Challenge c); }

    private List<Challenge> items;
    private Listener listener;

    public ChallengesAdapter(List<Challenge> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_challenge, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Challenge c = items.get(position);
        holder.tvName.setText(c.getName());
        holder.tvChallengeLevel.setText(c.getLevel());
        holder.tvChallengeUsers.setText(String.valueOf(c.getUsersCount()) + " users");
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetailsClicked(c);
        });
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    public void setItems(List<Challenge> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvChallengeLevel, tvChallengeUsers;
        Button btnDetails;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChallengeName);
            tvChallengeLevel = itemView.findViewById(R.id.tvChallengeLevel);
            tvChallengeUsers = itemView.findViewById(R.id.tvChallengeUsers);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
