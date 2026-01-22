package com.beeproductive.android.processors;

import android.content.Context;
import android.content.SharedPreferences;

import com.beeproductive.android.models.Challenge;

import java.util.Calendar;

/**
 * Processor for SCREEN_TIME_REDUCTION challenges
 * Checks if user reduced screen time by the required percentage compared to baseline
 */
public class ScreenTimeReductionProcessor extends BaseChallengeProcessor {

    private static final String PREFS_NAME = "ScreenTimeBaseline";
    private final SharedPreferences prefs;

    public ScreenTimeReductionProcessor(Context context) {
        super(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String processChallenge(Challenge challenge) {
        // Only process if challenge has started
        if (!hasChallengeStarted(challenge)) {
            log("Challenge " + challenge.getName() + " hasn't started yet");
            return null;
        }

        // Get or calculate baseline
        long baseline = getOrCalculateBaseline(challenge);
        if (baseline <= 0) {
            log("Cannot evaluate reduction challenge without baseline");
            return null;
        }

        // Check if challenge has ended
        if (hasChallengeEnded(challenge)) {
            return evaluateFinalStatus(challenge, baseline);
        }

        // Challenge is active, check progress
        Integer reductionPercentage = challenge.getReductionPercentage();
        if (reductionPercentage == null) {
            log("No reduction percentage set for challenge: " + challenge.getName());
            return null;
        }

        // Get average daily screen time during challenge
        long averageScreenTime = getAverageDailyScreenTimeDuringChallenge(challenge);
        long targetScreenTime = baseline * (100 - reductionPercentage) / 100;

        log("Screen Time Reduction Challenge: " + challenge.getName() +
            " - Baseline: " + baseline + "min, Current Avg: " + averageScreenTime +
            "min, Target: " + targetScreenTime + "min (" + reductionPercentage + "% reduction)");

        // Check if consistently exceeding target
        long todayScreenTime = getTodayScreenTimeMinutes();
        if (todayScreenTime > baseline * 1.5) { // Significantly exceeded baseline
            log("Screen time significantly exceeded baseline! Challenge may fail.");
        }

        return null; // Status only changes at end
    }

    /**
     * Get or calculate baseline screen time before challenge started
     */
    private long getOrCalculateBaseline(Challenge challenge) {
        String key = "baseline_" + challenge.getId();
        long savedBaseline = prefs.getLong(key, 0);

        if (savedBaseline > 0) {
            return savedBaseline;
        }

        // Calculate baseline: average screen time 7 days before challenge start
        try {
            String[] parts = challenge.getStartDate().split("-");
            Calendar startDate = Calendar.getInstance();
            startDate.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]), 0, 0, 0);

            Calendar baselineStart = (Calendar) startDate.clone();
            baselineStart.add(Calendar.DAY_OF_MONTH, -7);

            long screenTime = getScreenTimeForPeriod(baselineStart.getTimeInMillis(), startDate.getTimeInMillis());
            long baseline = screenTime / 7; // Average per day

            // Save baseline
            prefs.edit().putLong(key, baseline).apply();
            log("Calculated baseline for " + challenge.getName() + ": " + baseline + "min/day");

            return baseline;
        } catch (Exception e) {
            log("Error calculating baseline: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get average daily screen time during the challenge period
     */
    private long getAverageDailyScreenTimeDuringChallenge(Challenge challenge) {
        try {
            String[] startParts = challenge.getStartDate().split("-");
            Calendar startDate = Calendar.getInstance();
            startDate.set(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]) - 1,
                         Integer.parseInt(startParts[2]), 0, 0, 0);

            long startMillis = startDate.getTimeInMillis();
            long endMillis = System.currentTimeMillis();

            long totalScreenTime = getScreenTimeForPeriod(startMillis, endMillis);
            long daysPassed = (endMillis - startMillis) / (1000 * 60 * 60 * 24);

            if (daysPassed == 0) daysPassed = 1; // Avoid division by zero

            return totalScreenTime / daysPassed;
        } catch (Exception e) {
            log("Error calculating average screen time: " + e.getMessage());
            return getTodayScreenTimeMinutes(); // Fallback to today's time
        }
    }

    /**
     * Evaluate final status when challenge ends
     */
    private String evaluateFinalStatus(Challenge challenge, long baseline) {
        // If already failed, keep failed status
        if ("FAILED".equals(challenge.getStatus())) {
            return "FAILED";
        }

        Integer reductionPercentage = challenge.getReductionPercentage();
        if (reductionPercentage == null) {
            return null;
        }

        long averageScreenTime = getAverageDailyScreenTimeDuringChallenge(challenge);
        long targetScreenTime = baseline * (100 - reductionPercentage) / 100;

        if (averageScreenTime <= targetScreenTime) {
            log("Screen Time Reduction Challenge COMPLETED: " + challenge.getName() +
                " - Achieved " + averageScreenTime + "min avg vs target " + targetScreenTime + "min");
            return "COMPLETED";
        } else {
            log("Screen Time Reduction Challenge FAILED: " + challenge.getName() +
                " - Average " + averageScreenTime + "min exceeded target " + targetScreenTime + "min");
            return "FAILED";
        }
    }
}
