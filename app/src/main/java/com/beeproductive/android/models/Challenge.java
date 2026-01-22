package com.beeproductive.android.models;

public class Challenge {
    private String id;
    private String name;
    private String level;
    private int usersCount;
    private String description;

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

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
    public int getUsersCount() { return usersCount; }
    public String getDescription() { return description; }
}
