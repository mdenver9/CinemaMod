package com.cinemamod.bukkit.theater;

import org.bukkit.Location;
import org.bukkit.World;

public class TheaterRegion {
    private final Location firstLocation_;
    private final Location secondLocation_;

    public TheaterRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        firstLocation_ = new Location(world, x1, y1, z1);
        secondLocation_ = new Location(world, x2, y2, z2);
    }

    public boolean contains(Location location) {
        if (location.getWorld() != null) {
            if (!location.getWorld().getName().equals(firstLocation_.getWorld().getName())) {
                return false;
            }
        }

        int minX = Math.min(firstLocation_.getBlockX(), secondLocation_.getBlockX());
        int minY = Math.min(firstLocation_.getBlockY(), secondLocation_.getBlockY());
        int minZ = Math.min(firstLocation_.getBlockZ(), secondLocation_.getBlockZ());

        int maxX = Math.max(firstLocation_.getBlockX(), secondLocation_.getBlockX());
        int maxY = Math.max(firstLocation_.getBlockY(), secondLocation_.getBlockY());
        int maxZ = Math.max(firstLocation_.getBlockZ(), secondLocation_.getBlockZ());

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Location getFirstLocation() {
        return firstLocation_;
    }

    public Location getSecondLocation() {
        return secondLocation_;
    }
}
