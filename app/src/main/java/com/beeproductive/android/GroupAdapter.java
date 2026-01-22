package com.beeproductive.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onViewClicked(GroupItem item);
    }

    private final List<GroupItem> items;
    private final OnItemClickListener listener;

    public GroupAdapter(List<GroupItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvUsers.setText("Users: " + item.usersCount);
        holder.tvCode.setText("Code: " + item.code);
        holder.btnView.setOnClickListener(v -> listener.onViewClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUsers, tvCode;
        Button btnView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGroupName);
            tvUsers = itemView.findViewById(R.id.tvGroupUsers);
            tvCode = itemView.findViewById(R.id.tvGroupCode);
            btnView = itemView.findViewById(R.id.btnViewGroup);
        }
    }
}
