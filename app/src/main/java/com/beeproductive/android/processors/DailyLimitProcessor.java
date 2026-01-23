package com.beeproductive.android.processors;

import android.content.Context;

import com.beeproductive.android.models.Challenge;

/**
 * Processor for DAILY_LIMIT challenges
 * Checks if user's daily screen time exceeds the maximum allowed minutes
 */
public class DailyLimitProcessor extends BaseChallengeProcessor {

    public DailyLimitProcessor(Context context) {
        super(context);
    }

    @Override
    public String processChallenge(Challenge challenge) {
        // Only process if challenge has started
        if (!hasChallengeStarted(challenge)) {
            log("Challenge " + challenge.getName() + " hasn't started yet");
            return null;
        }

        // Check if challenge has ended
        if (hasChallengeEnded(challenge)) {
            // Challenge ended, determine if completed or failed
            return evaluateFinalStatus(challenge);
        }

        // Challenge is active, check daily compliance
        Integer maxDailyMinutes = challenge.getMaxDailyMinutes();
        if (maxDailyMinutes == null) {
            log("No maxDailyMinutes set for challenge: " + challenge.getName());
            return null;
        }

        long todayScreenTime = getTodayScreenTimeMinutes();
        log("Daily Limit Challenge: " + challenge.getName() +
            " - Screen time today: " + todayScreenTime + "min / Limit: " + maxDailyMinutes + "min");

        // Check if user exceeded limit today
        if (todayScreenTime > maxDailyMinutes) {
            log("User exceeded daily limit! Challenge failed.");
            return "FAILED";
        }

        // Still within limit
        return null; // No status change
    }

    /**
     * Evaluate final status when challenge ends
     * For daily limit, we check if user stayed within limit throughout the challenge
     */
    private String evaluateFinalStatus(Challenge challenge) {
        // If already failed, keep failed status
        if ("FAILED".equals(challenge.getStatus())) {
            return "FAILED";
        }

        // If challenge ended and user never failed, it's completed
        log("Daily Limit Challenge completed: " + challenge.getName());
        return "COMPLETED";
    }
}
