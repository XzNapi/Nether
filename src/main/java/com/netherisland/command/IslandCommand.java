package com.netherisland.command;

import com.netherisland.NetherIsland;
import com.netherisland.data.IslandData;
import com.netherisland.manager.IslandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandCommand implements CommandExecutor {
    private final NetherIsland plugin;
    private final IslandManager islandManager;
    private final Map<UUID, Long> deleteConfirmations;

    public IslandCommand(NetherIsland plugin) {
        this.plugin = plugin;
        this.islandManager = plugin.getIslandManager();
        this.deleteConfirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
            case "home":
                handleHome(player);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player) {
        if (islandManager.hasIsland(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("island-already-exists"));
            return;
        }

        IslandData island = islandManager.createIsland(player);
        if (island != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("island-created"));
        }
    }

    private void handleHome(Player player) {
        IslandData island = islandManager.getIsland(player.getUniqueId());
        
        if (island == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("island-not-found"));
            return;
        }

        player.teleport(island.getSpawnPoint());
        player.sendMessage(plugin.getConfigManager().getMessage("island-teleported"));
    }

    private void handleDelete(Player player, String[] args) {
        IslandData island = islandManager.getIsland(player.getUniqueId());
        
        if (island == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("island-not-found"));
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            deleteConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(plugin.getConfigManager().getMessage("confirm-delete"));
            return;
        }

        Long confirmTime = deleteConfirmations.get(player.getUniqueId());
        if (confirmTime == null || System.currentTimeMillis() - confirmTime > 30000) {
            player.sendMessage(plugin.getConfigManager().getMessage("confirm-delete"));
            deleteConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            return;
        }

        islandManager.deleteIsland(player, island.getCenter());
        deleteConfirmations.remove(player.getUniqueId());
        player.sendMessage(plugin.getConfigManager().getMessage("island-deleted"));
    }

    private void sendHelp(Player player) {
        player.sendMessage("§c§l[NetherIsland Commands]");
        player.sendMessage("§e/island create §7- Create a new island");
        player.sendMessage("§e/island home §7- Teleport to your island");
        player.sendMessage("§e/island delete §7- Delete your island permanently");
    }
}