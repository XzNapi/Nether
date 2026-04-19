package com.netherisland.data;

import lombok.Data;
import org.bukkit.Location;
import java.util.UUID;

@Data
public class IslandData {
    private UUID owner;
    private Location center;
    private Location spawnPoint;
    private long createdTime;
    private boolean hasStarterChest;

    public IslandData(UUID owner, Location center, Location spawnPoint) {
        this.owner = owner;
        this.center = center;
        this.spawnPoint = spawnPoint;
        this.createdTime = System.currentTimeMillis();
        this.hasStarterChest = true;
    }

    public IslandData() {
    }
}