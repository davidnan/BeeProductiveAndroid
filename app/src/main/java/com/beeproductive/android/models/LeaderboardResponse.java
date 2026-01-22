package com.beeproductive.android.models;

import java.util.List;

public class LeaderboardResponse {
    private String groupName;
    private String groupCode;
    private List<LeaderboardUserDto> leaderboard;

    public LeaderboardResponse() {
    }

    public LeaderboardResponse(String groupName, String groupCode, List<LeaderboardUserDto> leaderboard) {
        this.groupName = groupName;
        this.groupCode = groupCode;
        this.leaderboard = leaderboard;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public List<LeaderboardUserDto> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderboardUserDto> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
