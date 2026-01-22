package com.beeproductive.android.processors;

import android.content.Context;

import com.beeproductive.android.models.Challenge;

import java.util.List;

/**
 * Processor for APP_BLOCKING challenges
 * Checks if user used any of the blocked apps
 */
public class AppBlockingProcessor extends BaseChallengeProcessor {

    public AppBlockingProcessor(Context context) {
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
            return evaluateFinalStatus(challenge);
        }

        // Check if any blocked apps were used
        List<String> blockedApps = challenge.getBlockedApps();
        if (blockedApps == null || blockedApps.isEmpty()) {
            log("No blocked apps set for challenge: " + challenge.getName());
            return null;
        }

        // Check each blocked app
        for (String packageName : blockedApps) {
            if (wasAppUsedToday(packageName)) {
                log("App Blocking Challenge FAILED: User used blocked app " + packageName);
                return "FAILED";
            }
        }

        log("App Blocking Challenge: " + challenge.getName() + " - No blocked apps used today");
        return null; // Still compliant
    }

    /**
     * Evaluate final status when challenge ends
     */
    private String evaluateFinalStatus(Challenge challenge) {
        // If already failed, keep failed status
        if ("FAILED".equals(challenge.getStatus())) {
            return "FAILED";
        }

        // If challenge ended and user never failed, it's completed
        log("App Blocking Challenge completed: " + challenge.getName());
        return "COMPLETED";
    }
}
