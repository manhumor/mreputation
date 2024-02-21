package me.manhumor.mreputation;

import java.util.HashMap;

public class CooldownManager {
    public static HashMap<String, Long> cooldowns = new HashMap<>();
    public static long cooldownTime;
    public CooldownManager() {
        cooldownTime = MReputation.getInstance().getConfig().getLong("cooldown-time");
    }

    public static void addCooldown(String playerName) {
        cooldowns.put(playerName, 5000L + System.currentTimeMillis());
    }

    public static String getCooldown(String playerName) {
        long remaining = cooldowns.getOrDefault(playerName, 0L) - System.currentTimeMillis();
        return Long.toString(remaining / 1000L);
    }

    public static Boolean checkCooldown(String playerName) {
        return System.currentTimeMillis() > cooldowns.getOrDefault(playerName, 0L);
    }
}
