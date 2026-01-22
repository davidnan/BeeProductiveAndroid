package com.beeproductive.android.processors;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.beeproductive.android.models.Challenge;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Base class for all challenge processors
 * Handles common functionality like usage stats retrieval
 */
public abstract class BaseChallengeProcessor {

    protected static final String TAG = "ChallengeProcessor";
    protected final Context context;
    protected final UsageStatsManager usageStatsManager;

    public BaseChallengeProcessor(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    /**
     * Process a challenge and determine if status should change
     * @param challenge The challenge to process
     * @return New status (ENROLLED, COMPLETED, FAILED) or null if no change
     */
    public abstract String processChallenge(Challenge challenge);

    /**
     * Get total screen time for today in minutes
     */
    protected long getTodayScreenTimeMinutes() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, endOfDay);

        long totalTimeMillis = 0;
        for (UsageStats usageStat : stats.values()) {
            totalTimeMillis += usageStat.getTotalTimeInForeground();
        }

        return totalTimeMillis / (1000 * 60); // Convert to minutes
    }

    /**
     * Get screen time for specific apps today in minutes
     */
    protected long getAppsScreenTimeMinutes(List<String> packageNames) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, endOfDay);

        long totalTimeMillis = 0;
        for (String packageName : packageNames) {
            UsageStats usageStat = stats.get(packageName);
            if (usageStat != null) {
                totalTimeMillis += usageStat.getTotalTimeInForeground();
            }
        }

        return totalTimeMillis / (1000 * 60); // Convert to minutes
    }

    /**
     * Get screen time for a date range in minutes
     */
    protected long getScreenTimeForPeriod(long startMillis, long endMillis) {
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);

        long totalTimeMillis = 0;
        for (UsageStats usageStat : stats.values()) {
            totalTimeMillis += usageStat.getTotalTimeInForeground();
        }

        return totalTimeMillis / (1000 * 60); // Convert to minutes
    }

    /**
     * Check if a specific app was used today
     */
    protected boolean wasAppUsedToday(String packageName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, endOfDay);
        UsageStats usageStat = stats.get(packageName);

        return usageStat != null && usageStat.getTotalTimeInForeground() > 0;
    }

    /**
     * Check if challenge period has ended
     */
    protected boolean hasChallengeEnded(Challenge challenge) {
        if (challenge.getEndDate() == null || challenge.getEndDate().isEmpty()) {
            return false;
        }

        try {
            // Parse end date (format: YYYY-MM-DD)
            String[] parts = challenge.getEndDate().split("-");
            Calendar endDate = Calendar.getInstance();
            endDate.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]), 23, 59, 59);

            return System.currentTimeMillis() > endDate.getTimeInMillis();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing end date: " + challenge.getEndDate(), e);
            return false;
        }
    }

    /**
     * Check if challenge period has started
     */
    protected boolean hasChallengeStarted(Challenge challenge) {
        if (challenge.getStartDate() == null || challenge.getStartDate().isEmpty()) {
            return true; // Assume started if no start date
        }

        try {
            // Parse start date (format: YYYY-MM-DD)
            String[] parts = challenge.getStartDate().split("-");
            Calendar startDate = Calendar.getInstance();
            startDate.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]), 0, 0, 0);

            return System.currentTimeMillis() >= startDate.getTimeInMillis();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing start date: " + challenge.getStartDate(), e);
            return true;
        }
    }

    /**
     * Log processor activity
     */
    protected void log(String message) {
        Log.d(TAG, message);
    }
}
