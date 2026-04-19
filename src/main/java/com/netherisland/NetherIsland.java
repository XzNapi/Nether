package com.netherisland;

import com.netherisland.command.IslandCommand;
import com.netherisland.listener.IslandListener;
import com.netherisland.manager.ConfigManager;
import com.netherisland.manager.DataManager;
import com.netherisland.manager.IslandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class NetherIsland extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private IslandManager islandManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.islandManager = new IslandManager(this);
        
        getCommand("island").setExecutor(new IslandCommand(this));
        Bukkit.getPluginManager().registerEvents(new IslandListener(this), this);
        
        getLogger().info("NetherIsland has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("NetherIsland has been disabled!");
    }
}