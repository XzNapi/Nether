package com.netherisland.util;

import com.cryptomorin.xseries.XMaterial;
import com.netherisland.NetherIsland;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StarterChestPopulator {
    
    public static void populateChest(Chest chest, NetherIsland plugin) {
        Inventory inv = chest.getInventory();
        ConfigurationSection items = plugin.getConfig().getConfigurationSection("starter-items");
        
        if (items == null) return;
        
        int slot = 0;
        
        int fishingRodAmount = items.getInt("fishing-rod", 1);
        if (fishingRodAmount > 0) {
            XMaterial.matchXMaterial("FISHING_ROD")
                .map(XMaterial::parseItem)
                .ifPresent(item -> {
                    item.setAmount(fishingRodAmount);
                    inv.setItem(slot, item);
                });
        }
        
        int iceAmount = items.getInt("ice", 2);
        if (iceAmount > 0) {
            XMaterial.matchXMaterial("ICE")
                .map(XMaterial::parseItem)
                .ifPresent(item -> {
                    item.setAmount(iceAmount);
                    inv.setItem(slot + 1, item);
                });
        }
        
        int seedsAmount = items.getInt("wheat-seeds", 1);
        if (seedsAmount > 0) {
            XMaterial.matchXMaterial("WHEAT_SEEDS")
                .map(XMaterial::parseItem)
                .ifPresent(item -> {
                    item.setAmount(seedsAmount);
                    inv.setItem(slot + 2, item);
                });
        }
        
        int hoeAmount = items.getInt("wooden-hoe", 1);
        if (hoeAmount > 0) {
            XMaterial.matchXMaterial("WOODEN_HOE")
                .map(XMaterial::parseItem)
                .ifPresent(item -> {
                    item.setAmount(hoeAmount);
                    inv.setItem(slot + 3, item);
                });
        }
        
        int breadAmount = items.getInt("bread", 16);
        if (breadAmount > 0) {
            XMaterial.matchXMaterial("BREAD")
                .map(XMaterial::parseItem)
                .ifPresent(item -> {
                    item.setAmount(breadAmount);
                    inv.setItem(slot + 4, item);
                });
        }
        
        chest.update();
    }
}