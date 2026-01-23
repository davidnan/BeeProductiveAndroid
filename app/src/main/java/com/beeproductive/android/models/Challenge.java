package com.beeproductive.android.models;

import java.util.List;

public class Challenge {
    private String id;
    private String name;
    private String description;
    private String type;
    private String startDate;
    private String endDate;
    private int rewardBees;

    // Type-specific fields
    private Integer reductionPercentage;
    private List<String> blockedApps;
    private Integer maxDailyMinutes;
    private boolean isEnrolled;
    private String status; // ENROLLED, COMPLETED, FAILED

    // Deprecated fields for backward compatibility
    private String level;
    private int usersCount;

    public Challenge(String id, String name, String level) {
        this(id, name, level, 0, "");
    }

    public Challenge(String id, String name, String level, int usersCount, String description) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.usersCount = usersCount;
        this.description = description;
    }

    // New constructor for API response
    public Challenge(String id, String name, String description, String type,
                     String startDate, String endDate, int rewardBees) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rewardBees = rewardBees;
        this.level = mapTypeToLevel(type);
        this.usersCount = 0;
    }

    // Full constructor with type-specific fields
    public Challenge(String id, String name, String description, String type,
                     String startDate, String endDate, int rewardBees,
                     Integer reductionPercentage, List<String> blockedApps, Integer maxDailyMinutes,
                     boolean isEnrolled) {
        this(id, name, description, type, startDate, endDate, rewardBees);
        this.reductionPercentage = reductionPercentage;
        this.blockedApps = blockedApps;
        this.maxDailyMinutes = maxDailyMinutes;
        this.isEnrolled = isEnrolled;
    }

    private String mapTypeToLevel(String type) {
        if (type == null) return "Medium";
        switch (type) {
            case "DAILY_LIMIT":
                return "Daily Limit";
            case "APP_BLOCKING":
                return "App Blocking";
            case "SCREEN_TIME_REDUCTION":
                return "Screen Reduction";
            default:
                return type;
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
    public int getUsersCount() { return usersCount; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getRewardBees() { return rewardBees; }
    public Integer getReductionPercentage() { return reductionPercentage; }
    public List<String> getBlockedApps() { return blockedApps; }
    public Integer getMaxDailyMinutes() { return maxDailyMinutes; }
    public boolean isEnrolled() { return isEnrolled; }
    public String getStatus() { return status; }

    public void setUsersCount(int usersCount) { this.usersCount = usersCount; }
    public void setReductionPercentage(Integer reductionPercentage) { this.reductionPercentage = reductionPercentage; }
    public void setBlockedApps(List<String> blockedApps) { this.blockedApps = blockedApps; }
    public void setMaxDailyMinutes(Integer maxDailyMinutes) { this.maxDailyMinutes = maxDailyMinutes; }
    public void setStatus(String status) { this.status = status; }
}
