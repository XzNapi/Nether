package com.netherisland.manager;

import com.netherisland.NetherIsland;
import com.netherisland.data.IslandData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final NetherIsland plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;
    private final Map<UUID, IslandData> islands;

    public DataManager(NetherIsland plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        this.islands = new HashMap<>();
        
        loadIslands();
    }

    public void saveIsland(IslandData island) {
        String key = "islands." + island.getOwner().toString();
        dataConfig.set(key + ".owner", island.getOwner().toString());
        dataConfig.set(key + ".center", island.getCenter());
        dataConfig.set(key + ".spawnPoint", island.getSpawnPoint());
        dataConfig.set(key + ".createdTime", island.getCreatedTime());
        dataConfig.set(key + ".hasStarterChest", island.isHasStarterChest());
        
        islands.put(island.getOwner(), island);
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save island data: " + e.getMessage());
        }
    }

    public void deleteIsland(UUID owner) {
        String key = "islands." + owner.toString();
        dataConfig.set(key, null);
        
        islands.remove(owner);
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not delete island data: " + e.getMessage());
        }
    }

    public IslandData getIsland(UUID owner) {
        return islands.get(owner);
    }

    public boolean hasIsland(UUID owner) {
        return islands.containsKey(owner);
    }

    public Collection<IslandData> getIslands() {
        return islands.values();
    }

    private void loadIslands() {
        islands.clear();
        
        if (!dataConfig.contains("islands")) {
            return;
        }
        
        for (String key : dataConfig.getKeys(false)) {
            if (key.equals("islands")) {
                for (String islandKey : dataConfig.getConfigurationSection("islands").getKeys(false)) {
                    String fullKey = "islands." + islandKey;
                    
                    UUID owner = UUID.fromString(dataConfig.getString(fullKey + ".owner"));
                    Object centerObj = dataConfig.get(fullKey + ".center");
                    Object spawnPointObj = dataConfig.get(fullKey + ".spawnPoint");
                    
                    if (centerObj instanceof org.bukkit.Location && spawnPointObj instanceof org.bukkit.Location) {
                        org.bukkit.Location center = (org.bukkit.Location) centerObj;
                        org.bukkit.Location spawnPoint = (org.bukkit.Location) spawnPointObj;
                        
                        IslandData island = new IslandData(owner, center, spawnPoint);
                        island.setCreatedTime(dataConfig.getLong(fullKey + ".createdTime"));
                        island.setHasStarterChest(dataConfig.getBoolean(fullKey + ".hasStarterChest"));
                        
                        islands.put(owner, island);
                    }
                }
            }
        }
    }

    public void saveAll() {
        for (IslandData island : islands.values()) {
            saveIsland(island);
        }
    }
}