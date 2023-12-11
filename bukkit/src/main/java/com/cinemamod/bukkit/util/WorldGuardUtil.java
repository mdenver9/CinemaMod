package com.cinemamod.bukkit.util;

import com.cinemamod.bukkit.theater.Theater;
import com.cinemamod.bukkit.theater.TheaterRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public final class WorldGuardUtil {
    public static List<TheaterRegion> theaterRegions = new ArrayList<>();

    public static Set<Player> getPlayersInRegion(TheaterRegion region) {
        Set<Player> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();
            if (region.contains(location)) {
                players.add(player);
            }
        }
        return players;
    }

    public static String getRandomId() {
        Random ran = new Random();
        int i = ran.nextInt(99999);
        return UUID.randomUUID().toString() + i;
    }
}
