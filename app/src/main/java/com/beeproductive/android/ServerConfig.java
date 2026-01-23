package com.beeproductive.android;

/**
 * Server configuration constants for API communication.
 *
 * Note: Android emulators cannot access localhost directly.
 * Use 10.0.2.2 to access the host machine's localhost:8080
 * For physical devices, use your computer's actual IP address on the local network.
 */
public class ServerConfig {

    // Base URL for Android Emulator (maps to host machine's localhost:8080)
    public static final String BASE_URL = "http://10.0.2.2:8080";
//    public static final String BASE_URL = "http://localhost:8080";

    // Alternative for physical device testing (uncomment and replace with your IP)
    // public static final String BASE_URL = "http://192.168.1.XXX:8080";

    // API endpoints

    // Example endpoint paths (customize as needed)
    public static final String ENDPOINT_CHALLENGES = BASE_URL + "/challenge";
    public static final String ENDPOINT_MY_CHALLENGES = ENDPOINT_CHALLENGES + "/my-challenges";
    public static final String ENDPOINT_CHALLENGES_JOIN = ENDPOINT_CHALLENGES + "/join";
    public static final String ENDPOINT_CHALLENGES_GET = ENDPOINT_CHALLENGES; // + "/{challengeId}";
    public static final String ENDPOINT_CHALLENGE_UPDATE = ENDPOINT_CHALLENGES + "/update-status"; // + "/{challengeId}";


    public static final String ENDPOINT_GROUPS = BASE_URL + "/group";
    public static final String ENDPOINT_GROUPS_CREATE = ENDPOINT_GROUPS + "/create";
    public static final String ENDPOINT_GROUPS_JOIN = ENDPOINT_GROUPS + "/join";
    public static final String ENDPOINT_GROUPS_GET = ENDPOINT_GROUPS; // + "/{groupId}";
    public static final String ENDPOINT_MY_GROUPS = ENDPOINT_GROUPS + "/my-groups";
    public static final String ENDPOINT_LEADERBOARD = ENDPOINT_GROUPS + "/leaderboard"; // + "/{groupCode}"

    // Connection timeout settings (in milliseconds)
    public static final int CONNECT_TIMEOUT = 30000; // 30 seconds
    public static final int READ_TIMEOUT = 30000;    // 30 seconds
    public static final int WRITE_TIMEOUT = 30000;   // 30 seconds

    // Private constructor to prevent instantiation
    private ServerConfig() {
        throw new AssertionError("Cannot instantiate ServerConfig class");
    }
}
