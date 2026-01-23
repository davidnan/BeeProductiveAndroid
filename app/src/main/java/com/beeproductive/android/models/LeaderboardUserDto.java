package com.beeproductive.android.models;

public class LeaderboardUserDto {
    private String name;
    private int numberOfBees;
    private int rank;

    public LeaderboardUserDto() {
    }

    public LeaderboardUserDto(String name, int numberOfBees, int rank) {
        this.name = name;
        this.numberOfBees = numberOfBees;
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfBees() {
        return numberOfBees;
    }

    public void setNumberOfBees(int numberOfBees) {
        this.numberOfBees = numberOfBees;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
