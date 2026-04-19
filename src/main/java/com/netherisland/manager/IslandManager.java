package com.netherisland.manager;

import com.cryptomorin.xseries.XMaterial;
import com.netherisland.NetherIsland;
import com.netherisland.data.IslandData;
import com.netherisland.util.StarterChestPopulator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IslandManager {
    private final NetherIsland plugin;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private int islandCounter;

    public IslandManager(NetherIsland plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.configManager = plugin.getConfigManager();
        this.islandCounter = 0;
    }

    public IslandData createIsland(Player player) {
        World nether = Bukkit.getWorld(player.getWorld().getName().replace("_nether", "") + "_nether");
        if (nether == null) {
            nether = Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NETHER)
                .findFirst()
                .orElse(null);
        }
        
        if (nether == null) {
            player.sendMessage(configManager.getMessage("nether-not-found"));
            return null;
        }
        
        int spacing = configManager.getIslandSpacing();
        int x = (islandCounter % 10) * spacing;
        int z = (islandCounter / 10) * spacing;
        islandCounter++;
        
        Location center = new Location(nether, x, 64, z);
        generateIsland(center);
        
        Location spawnPoint = center.clone().add(0, 1, 0);
        spawnPoint.setYaw(0);
        spawnPoint.setPitch(0);
        
        IslandData island = new IslandData(player.getUniqueId(), center, spawnPoint);
        dataManager.saveIsland(island);
        
        player.teleport(spawnPoint);
        return island;
    }

    private void generateIsland(Location center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = center.clone().add(x, 0, z).getBlock();
                block.setType(Material.GRASS_BLOCK);
            }
        }
        
        Block treeBase = center.clone().add(1, 1, 1).getBlock();
        treeBase.setType(Material.OAK_LOG);
        
        Block leaves1 = treeBase.getRelative(0, 1, 0);
        leaves1.setType(Material.OAK_LEAVES);
        Block leaves2 = treeBase.getRelative(0, 2, 0);
        leaves2.setType(Material.OAK_LEAVES);
        Block leaves3 = treeBase.getRelative(1, 1, 0);
        leaves3.setType(Material.OAK_LEAVES);
        Block leaves4 = treeBase.getRelative(-1, 1, 0);
        leaves4.setType(Material.OAK_LEAVES);
        Block leaves5 = treeBase.getRelative(0, 1, 1);
        leaves5.setType(Material.OAK_LEAVES);
        Block leaves6 = treeBase.getRelative(0, 1, -1);
        leaves6.setType(Material.OAK_LEAVES);
        
        Block chestBlock = center.clone().add(-1, 1, -1).getBlock();
        chestBlock.setType(Material.CHEST);
        
        if (chestBlock.getState() instanceof Chest) {
            Chest chest = (Chest) chestBlock.getState();
            StarterChestPopulator.populateChest(chest, plugin);
        }
    }

    public void deleteIsland(Player player, Location center) {
        int size = configManager.getIslandSize();
        int radius = size / 2;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    block.setType(Material.AIR);
                }
            }
        }
        
        dataManager.deleteIsland(player.getUniqueId());
    }

    public IslandData getIsland(UUID owner) {
        return dataManager.getIsland(owner);
    }

    public boolean hasIsland(UUID owner) {
        return dataManager.hasIsland(owner);
    }

    public boolean isOnIsland(Location location, IslandData island) {
        if (!location.getWorld().equals(island.getCenter().getWorld())) {
            return false;
        }
        
        int size = configManager.getIslandSize();
        int radius = size / 2;
        
        double dx = Math.abs(location.getX() - island.getCenter().getX());
        double dy = Math.abs(location.getY() - island.getCenter().getY());
        double dz = Math.abs(location.getZ() - island.getCenter().getZ());
        
        return dx <= radius && dy <= radius && dz <= radius;
    }

    public IslandData getIslandAt(Location location) {
        for (IslandData island : dataManager.getIslands()) {
            if (isOnIsland(location, island)) {
                return island;
            }
        }
        return null;
    }
}