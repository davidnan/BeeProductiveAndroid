package com.beeproductive.android.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to map Android package names to readable app names
 */
public class AppNameMapper {

    private static final Map<String, String> APP_NAME_MAP = new HashMap<>();

    static {
        // Social Media Apps
        APP_NAME_MAP.put("com.zhiliaoapp.musically", "TikTok");
        APP_NAME_MAP.put("com.snapchat.android", "Snapchat");
        APP_NAME_MAP.put("com.twitter.android", "Twitter");
        APP_NAME_MAP.put("com.facebook.katana", "Facebook");
        APP_NAME_MAP.put("com.instagram.android", "Instagram");
        APP_NAME_MAP.put("com.reddit.frontpage", "Reddit");
        APP_NAME_MAP.put("com.pinterest", "Pinterest");
        APP_NAME_MAP.put("com.linkedin.android", "LinkedIn");
        APP_NAME_MAP.put("com.tumblr", "Tumblr");
        APP_NAME_MAP.put("com.discord", "Discord");

        // Gaming Apps
        APP_NAME_MAP.put("com.supercell.clashofclans", "Clash of Clans");
        APP_NAME_MAP.put("com.innersloth.spacemafia", "Among Us");
        APP_NAME_MAP.put("com.tencent.ig", "PUBG Mobile");
        APP_NAME_MAP.put("com.activision.callofduty.shooter", "Call of Duty Mobile");
        APP_NAME_MAP.put("com.king.candycrushsaga", "Candy Crush Saga");
        APP_NAME_MAP.put("com.ea.gp.fifamobile", "FIFA Mobile");
        APP_NAME_MAP.put("com.miHoYo.GenshinImpact", "Genshin Impact");
        APP_NAME_MAP.put("com.roblox.client", "Roblox");
        APP_NAME_MAP.put("com.mojang.minecraftpe", "Minecraft");
        APP_NAME_MAP.put("com.kiloo.subwaysurf", "Subway Surfers");

        // Streaming/Video Apps
        APP_NAME_MAP.put("com.netflix.mediaclient", "Netflix");
        APP_NAME_MAP.put("com.google.android.youtube", "YouTube");
        APP_NAME_MAP.put("com.amazon.avod.thirdpartyclient", "Prime Video");
        APP_NAME_MAP.put("com.hulu.plus", "Hulu");
        APP_NAME_MAP.put("com.disney.disneyplus", "Disney+");
        APP_NAME_MAP.put("com.spotify.music", "Spotify");
        APP_NAME_MAP.put("com.zhiliaoapp.musically", "TikTok");

        // Messaging Apps
        APP_NAME_MAP.put("com.whatsapp", "WhatsApp");
        APP_NAME_MAP.put("org.telegram.messenger", "Telegram");
        APP_NAME_MAP.put("com.facebook.orca", "Messenger");
        APP_NAME_MAP.put("com.viber.voip", "Viber");
        APP_NAME_MAP.put("jp.naver.line.android", "LINE");
        APP_NAME_MAP.put("com.skype.raider", "Skype");

        // Shopping Apps
        APP_NAME_MAP.put("com.amazon.mShop.android.shopping", "Amazon");
        APP_NAME_MAP.put("com.ebay.mobile", "eBay");
        APP_NAME_MAP.put("com.alibaba.aliexpresshd", "AliExpress");
        APP_NAME_MAP.put("com.shopify.mobile", "Shopify");
    }

    /**
     * Get the readable app name from package name
     * @param packageName Android package name
     * @return Readable app name, or simplified package name if not found
     */
    public static String getAppName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "Unknown App";
        }

        // Check if we have a mapping
        if (APP_NAME_MAP.containsKey(packageName)) {
            return APP_NAME_MAP.get(packageName);
        }

        // If no mapping exists, try to extract a readable name from package
        // e.g., "com.example.myapp" -> "Myapp"
        String[] parts = packageName.split("\\.");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // Capitalize first letter
            return lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1);
        }

        return packageName;
    }

    /**
     * Check if an app name exists in the mapping
     * @param packageName Android package name
     * @return true if mapping exists
     */
    public static boolean hasMapping(String packageName) {
        return APP_NAME_MAP.containsKey(packageName);
    }
}
