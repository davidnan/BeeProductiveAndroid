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
import com.beeproductive.android.utils.AppNameMapper;

import java.util.List;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.VH> {
    public interface Listener {
        void onDetailsClicked(Challenge c);
        void onJoinClicked(Challenge c);
    }

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

        // Show description
        if (c.getDescription() != null && !c.getDescription().isEmpty()) {
            holder.tvDescription.setText(c.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Show reward bees if available, otherwise show user count
        if (c.getRewardBees() > 0) {
            holder.tvChallengeUsers.setText(c.getRewardBees() + " 🐝 reward");
        } else {
            holder.tvChallengeUsers.setText(String.valueOf(c.getUsersCount()) + " users");
        }

        // Display type-specific information
        String specificInfo = getTypeSpecificInfo(c);
        if (specificInfo != null && !specificInfo.isEmpty()) {
            holder.tvSpecificInfo.setText(specificInfo);
            holder.tvSpecificInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvSpecificInfo.setVisibility(View.GONE);
        }

        // Display challenge dates
        String dates = getChallengeDates(c);
        if (dates != null && !dates.isEmpty()) {
            holder.tvDates.setText(dates);
            holder.tvDates.setVisibility(View.VISIBLE);
        } else {
            holder.tvDates.setVisibility(View.GONE);
        }

        // Update button based on enrollment status
        if (c.isEnrolled()) {
            // For enrolled challenges, check status
            String status = c.getStatus();

            if ("COMPLETED".equals(status)) {
                // Completed challenge - green button
                holder.btnJoin.setText("✓ Completed");
                holder.btnJoin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.green_completed)
                    )
                );
                holder.btnJoin.setTextColor(
                    holder.itemView.getContext().getColor(R.color.white)
                );
                holder.btnJoin.setEnabled(false);
            } else if ("FAILED".equals(status)) {
                // Failed challenge - red button
                holder.btnJoin.setText("✗ Failed");
                holder.btnJoin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.red_failed)
                    )
                );
                holder.btnJoin.setTextColor(
                    holder.itemView.getContext().getColor(R.color.white)
                );
                holder.btnJoin.setEnabled(false);
            } else {
                // ENROLLED status - gray button
                holder.btnJoin.setText("Enrolled");
                holder.btnJoin.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.gray_enrolled)
                    )
                );
                holder.btnJoin.setTextColor(
                    holder.itemView.getContext().getColor(R.color.brown)
                );
                holder.btnJoin.setEnabled(false);
            }
        } else {
            holder.btnJoin.setText("Join");
            holder.btnJoin.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getColor(R.color.honey_button)
                )
            );
            holder.btnJoin.setTextColor(
                holder.itemView.getContext().getColor(R.color.brown)
            );
            holder.btnJoin.setEnabled(true);
            holder.btnJoin.setOnClickListener(v -> {
                if (listener != null) listener.onJoinClicked(c);
            });
        }
    }

    private String getTypeSpecificInfo(Challenge c) {
        if (c.getType() == null) return null;

        switch (c.getType()) {
            case "DAILY_LIMIT":
                if (c.getMaxDailyMinutes() != null) {
                    int hours = c.getMaxDailyMinutes() / 60;
                    int minutes = c.getMaxDailyMinutes() % 60;
                    if (hours > 0 && minutes > 0) {
                        return "⏱ Limit: " + hours + "h " + minutes + "m per day";
                    } else if (hours > 0) {
                        return "⏱ Limit: " + hours + " hour" + (hours > 1 ? "s" : "") + " per day";
                    } else {
                        return "⏱ Limit: " + minutes + " minutes per day";
                    }
                }
                break;

            case "SCREEN_TIME_REDUCTION":
                if (c.getReductionPercentage() != null) {
                    return "📉 Reduce screen time by " + c.getReductionPercentage() + "%";
                }
                break;

            case "APP_BLOCKING":
                if (c.getBlockedApps() != null && !c.getBlockedApps().isEmpty()) {
                    StringBuilder apps = new StringBuilder("🚫 Blocked: ");
                    for (int i = 0; i < c.getBlockedApps().size(); i++) {
                        String packageName = c.getBlockedApps().get(i);
                        String appName = AppNameMapper.getAppName(packageName);
                        apps.append(appName);
                        if (i < c.getBlockedApps().size() - 1) {
                            apps.append(", ");
                        }
                    }
                    return apps.toString();
                }
                break;
        }

        return null;
    }

    private String getChallengeDates(Challenge c) {
        String startDate = c.getStartDate();
        String endDate = c.getEndDate();

        if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
            return null;
        }

        StringBuilder dates = new StringBuilder("📅 ");

        if (startDate != null && !startDate.isEmpty()) {
            dates.append(formatDate(startDate));
        }

        if (endDate != null && !endDate.isEmpty()) {
            if (startDate != null && !startDate.isEmpty()) {
                dates.append(" → ");
            }
            dates.append(formatDate(endDate));
        }

        return dates.toString();
    }

    private String formatDate(String dateString) {
        try {
            // Input formats:
            // "YYYY-MM-DD" or
            // "YYYY-MM-DDTHH:MM:SS" or
            // "YYYY-MM-DDTHH:MM:SS.sssZ"

            // Remove timezone indicator if present
            dateString = dateString.replace("Z", "");

            // Split by 'T' to separate date and time
            String[] dateTimeParts = dateString.split("T");
            String datePart = dateTimeParts[0];
            String timePart = dateTimeParts.length > 1 ? dateTimeParts[1] : null;

            // Parse date
            String[] dateParts = datePart.split("-");
            if (dateParts.length == 3) {
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);

                // Month names
                String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

                // Build formatted string
                StringBuilder formatted = new StringBuilder();
                formatted.append(monthNames[month - 1])
                        .append(" ")
                        .append(day)
                        .append(", ")
                        .append(year);

                // Add time if present
                if (timePart != null && !timePart.isEmpty()) {
                    // Parse time (HH:MM:SS or HH:MM:SS.sss)
                    String[] timeParts = timePart.split(":");
                    if (timeParts.length >= 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);

                        // Format time in 12-hour format with AM/PM
                        String amPm = hour >= 12 ? "PM" : "AM";
                        int hour12 = hour % 12;
                        if (hour12 == 0) hour12 = 12;

                        formatted.append(" ")
                                .append(hour12)
                                .append(":")
                                .append(String.format("%02d", minute))
                                .append(" ")
                                .append(amPm);
                    }
                }

                return formatted.toString();
            }
        } catch (Exception e) {
            // If parsing fails, return original string
        }
        return dateString;
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    public void setItems(List<Challenge> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvChallengeLevel, tvDescription, tvChallengeUsers, tvSpecificInfo, tvDates;
        Button btnJoin;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChallengeName);
            tvChallengeLevel = itemView.findViewById(R.id.tvChallengeLevel);
            tvDescription = itemView.findViewById(R.id.tvChallengeDescription);
            tvChallengeUsers = itemView.findViewById(R.id.tvChallengeUsers);
            tvSpecificInfo = itemView.findViewById(R.id.tvChallengeSpecificInfo);
            tvDates = itemView.findViewById(R.id.tvChallengeDates);
            btnJoin = itemView.findViewById(R.id.btnJoinChallenge);
        }
    }
}
