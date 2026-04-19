package com.netherisland.manager;

import com.netherisland.NetherIsland;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final NetherIsland plugin;
    private final FileConfiguration config;

    public ConfigManager(NetherIsland plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getIslandSpacing() {
        return config.getInt("settings.island-spacing", 1000);
    }

    public int getIslandSize() {
        return config.getInt("settings.island-size", 50);
    }

    public int getPlatformSize() {
        return config.getInt("settings.platform-size", 7);
    }

    public String getPlatformMaterial() {
        return config.getString("settings.platform-material", "NETHERRACK");
    }

    public int getStarterChestYOffset() {
        return config.getInt("settings.starter-chest-y-offset", 1);
    }

    public boolean isLavaFishingEnabled() {
        return config.getBoolean("lava-fishing.enabled", true);
    }

    public int getLavaFishingMinWait() {
        return config.getInt("lava-fishing.min-wait-time", 5);
    }

    public int getLavaFishingMaxWait() {
        return config.getInt("lava-fishing.max-wait-time", 30);
    }

    public boolean isNetherFarmingEnabled() {
        return config.getBoolean("nether-farming.enabled", true);
    }

    public int getLavaRange() {
        return config.getInt("nether-farming.lava-range", 4);
    }

    public boolean isMobSpawningEnabled() {
        return config.getBoolean("mob-spawning.enabled", true);
    }

    public boolean isOreGeneratorEnabled() {
        return config.getBoolean("ore-generator.enabled", true);
    }

    public boolean iceBreaksPermanently() {
        return config.getBoolean("ore-generator.ice-breaks-permanently", true);
    }

    public String getMessage(String key) {
        String prefix = config.getString("messages.prefix", "&c[NetherIsland]&r");
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return colorize(prefix + " " + message);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}