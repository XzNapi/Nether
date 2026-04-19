package com.netherisland.listener;

import com.cryptomorin.xseries.XMaterial;
import com.netherisland.NetherIsland;
import com.netherisland.data.IslandData;
import com.netherisland.manager.IslandManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandListener implements Listener {
    private final NetherIsland plugin;
    private final IslandManager islandManager;
    private final Random random;

    public IslandListener(NetherIsland plugin) {
        this.plugin = plugin;
        this.islandManager = plugin.getIslandManager();
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfigManager().isLavaFishingEnabled()) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        
        Player player = event.getPlayer();
        Location hookLoc = event.getHook().getLocation();
        
        if (hookLoc.getBlock().getType() != Material.LAVA) return;
        
        IslandData island = islandManager.getIslandAt(player.getLocation());
        if (island == null) return;
        
        event.setCancelled(true);
        
        ItemStack loot = getLavaFishingLoot();
        if (loot != null) {
            Item item = hookLoc.getWorld().dropItem(hookLoc, loot);
            item.setVelocity(player.getLocation().toVector().subtract(hookLoc.toVector()).normalize().multiply(0.3));
            player.sendMessage(plugin.getConfigManager().getMessage("lava-fishing-catch"));
        }
    }

    private ItemStack getLavaFishingLoot() {
        ConfigurationSection lootSection = plugin.getConfig().getConfigurationSection("lava-fishing.loot");
        if (lootSection == null) return null;
        
        List<LootEntry> entries = new ArrayList<>();
        int totalWeight = 0;
        
        for (String key : lootSection.getKeys(false)) {
            ConfigurationSection entry = lootSection.getConfigurationSection(key);
            if (entry == null) continue;
            
            String materialName = entry.getString("material");
            int weight = entry.getInt("weight", 1);
            int minAmount = entry.getInt("min-amount", 1);
            int maxAmount = entry.getInt("max-amount", 1);
            
            entries.add(new LootEntry(materialName, weight, minAmount, maxAmount));
            totalWeight += weight;
        }
        
        if (entries.isEmpty()) return null;
        
        int roll = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (LootEntry entry : entries) {
            currentWeight += entry.weight;
            if (roll < currentWeight) {
                int amount = entry.minAmount + random.nextInt(entry.maxAmount - entry.minAmount + 1);
                return XMaterial.matchXMaterial(entry.material)
                    .map(XMaterial::parseItem)
                    .map(item -> {
                        item.setAmount(amount);
                        return item;
                    })
                    .orElse(null);
            }
        }
        
        return null;
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!plugin.getConfigManager().isNetherFarmingEnabled()) return;
        
        Block block = event.getBlock();
        if (block.getWorld().getEnvironment() != World.Environment.NETHER) return;
        
        IslandData island = islandManager.getIslandAt(block.getLocation());
        if (island == null) return;
        
        List<String> allowedCrops = plugin.getConfig().getStringList("nether-farming.allowed-crops");
        String blockType = block.getType().name();
        
        boolean isAllowed = false;
        for (String crop : allowedCrops) {
            if (blockType.contains(crop)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) return;
        
        int lavaRange = plugin.getConfigManager().getLavaRange();
        boolean hasLavaNearby = false;
        
        for (int x = -lavaRange; x <= lavaRange; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -lavaRange; z <= lavaRange; z++) {
                    Block nearby = block.getRelative(x, y, z);
                    if (nearby.getType() == Material.LAVA) {
                        hasLavaNearby = true;
                        break;
                    }
                }
                if (hasLavaNearby) break;
            }
            if (hasLavaNearby) break;
        }
        
        if (!hasLavaNearby) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!plugin.getConfigManager().isMobSpawningEnabled()) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        
        Location loc = event.getLocation();
        if (loc.getWorld().getEnvironment() != World.Environment.NETHER) return;
        
        IslandData island = islandManager.getIslandAt(loc);
        if (island == null) return;
        
        List<String> allowedMobs = plugin.getConfig().getStringList("mob-spawning.allowed-mobs");
        String mobType = event.getEntityType().name();
        
        boolean isAllowed = allowedMobs.contains(mobType);
        if (!isAllowed) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!plugin.getConfigManager().isOreGeneratorEnabled()) return;
        
        Block from = event.getBlock();
        Block to = event.getToBlock();
        
        if (from.getType() != Material.LAVA) return;
        
        Material toType = to.getType();
        XMaterial xToMaterial = XMaterial.matchXMaterial(toType);
        boolean isIce = xToMaterial.name().contains("ICE");
        
        if (!isIce) return;
        
        IslandData island = islandManager.getIslandAt(to.getLocation());
        if (island == null) return;
        
        event.setCancelled(true);
        
        Material generatedOre = getGeneratedOre();
        to.setType(generatedOre);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if (!plugin.getConfigManager().isOreGeneratorEnabled()) return;
        
        Block block = event.getBlock();
        
        if (block.getType() != Material.LAVA) return;
        
        IslandData island = islandManager.getIslandAt(block.getLocation());
        if (island == null) return;
        
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block nearby = block.getRelative(x, y, z);
                    XMaterial xMaterial = XMaterial.matchXMaterial(nearby.getType());
                    if (xMaterial.name().contains("ICE")) {
                        event.setCancelled(true);
                        Material generatedOre = getGeneratedOre();
                        block.setType(generatedOre);
                        return;
                    }
                }
            }
        }
    }

    private Material getGeneratedOre() {
        ConfigurationSection oreSection = plugin.getConfig().getConfigurationSection("ore-generator.chances");
        if (oreSection == null) return Material.COBBLESTONE;
        
        List<OreEntry> entries = new ArrayList<>();
        int totalChance = 0;
        
        for (String key : oreSection.getKeys(false)) {
            ConfigurationSection entry = oreSection.getConfigurationSection(key);
            if (entry == null) continue;
            
            String materialName = entry.getString("material");
            int chance = entry.getInt("chance", 1);
            
            entries.add(new OreEntry(materialName, chance));
            totalChance += chance;
        }
        
        if (entries.isEmpty()) return Material.COBBLESTONE;
        
        int roll = random.nextInt(totalChance);
        int currentChance = 0;
        
        for (OreEntry entry : entries) {
            currentChance += entry.chance;
            if (roll < currentChance) {
                return XMaterial.matchXMaterial(entry.material)
                    .map(XMaterial::parseMaterial)
                    .orElse(Material.COBBLESTONE);
            }
        }
        
        return Material.COBBLESTONE;
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        XMaterial xMaterial = XMaterial.matchXMaterial(block.getType());
        
        if (!xMaterial.name().contains("ICE")) return;
        
        if (block.getWorld().getEnvironment() != World.Environment.NETHER) return;
        
        IslandData island = islandManager.getIslandAt(block.getLocation());
        if (island == null) return;
        
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().iceBreaksPermanently()) return;
        
        Block block = event.getBlock();
        XMaterial xMaterial = XMaterial.matchXMaterial(block.getType());
        
        if (!xMaterial.name().contains("ICE")) return;
        
        if (block.getWorld().getEnvironment() != World.Environment.NETHER) return;
        
        IslandData island = islandManager.getIslandAt(block.getLocation());
        if (island == null) return;
        
        event.setDropItems(false);
    }

    private static class LootEntry {
        String material;
        int weight;
        int minAmount;
        int maxAmount;

        LootEntry(String material, int weight, int minAmount, int maxAmount) {
            this.material = material;
            this.weight = weight;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    private static class OreEntry {
        String material;
        int chance;

        OreEntry(String material, int chance) {
            this.material = material;
            this.chance = chance;
        }
    }
}