package com.cinemamod.bukkit.theater;

import com.cinemamod.bukkit.CinemaModPlugin;
import com.cinemamod.bukkit.theater.screen.PreviewScreen;
import com.cinemamod.bukkit.theater.screen.Screen;
import com.cinemamod.bukkit.util.WorldGuardUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TheaterManager {

    private CinemaModPlugin cinemaModPlugin;
    private List<Theater> theaters;

    public TheaterManager(CinemaModPlugin cinemaModPlugin) {
        this.cinemaModPlugin = cinemaModPlugin;
        theaters = new ArrayList<>();
    }

    public List<Theater> getTheaters() {
        return theaters;
    }

    public void tickTheaters() {
        for (Theater theater : theaters) {
            theater.tick(cinemaModPlugin);
        }
    }

    public Theater getCurrentTheater(Player player) {
        for (Theater theater : theaters) {
            if (theater.isViewer(player)) {
                return theater;
            }
        }

        return null;
    }

    public Theater getCurrentTheaterByName(String name) {
        for (Theater theater : theaters) {
            if (theater.getName().equals(name)) {
                return theater;
            }
        }

        return null;
    }

    public synchronized void loadFromConfig(@Nullable ConfigurationSection theaterSection) {
        List<Theater> theaters = new ArrayList<>();
        if (theaterSection == null) return;

        for (String theaterId : theaterSection.getKeys(false)) {
            String theaterName = theaterSection.getString(theaterId + ".name");
            boolean theaterHidden = theaterSection.getBoolean(theaterId + ".hidden");
            String theaterType = theaterSection.getString(theaterId + ".type");
            String screenWorld = theaterSection.getString(theaterId + ".screen.world");
            int screenX = theaterSection.getInt(theaterId + ".screen.x");
            int screenY = theaterSection.getInt(theaterId + ".screen.y");
            int screenZ = theaterSection.getInt(theaterId + ".screen.z");
            String screenFacing = theaterSection.getString(theaterId + ".screen.facing");
            float screenWidth = (float) theaterSection.getDouble(theaterId + ".screen.width");
            float screenHeight = (float) theaterSection.getDouble(theaterId + ".screen.height");
            boolean screenVisible = theaterSection.getBoolean(theaterId + ".screen.visible");
            boolean screenMuted = theaterSection.getBoolean(theaterId + ".screen.muted");
            Screen screen = new Screen(screenWorld, screenX, screenY, screenZ, screenFacing, screenWidth, screenHeight, screenVisible, screenMuted);

            String regionWorld = theaterSection.getString(theaterId + ".first_location.world");
            int regionX1 = theaterSection.getInt(theaterId + ".first_location.x");
            int regionY1 = theaterSection.getInt(theaterId + ".first_location.y");
            int regionZ1 = theaterSection.getInt(theaterId + ".first_location.z");
            int regionX2 = theaterSection.getInt(theaterId + ".second_location.x");
            int regionY2 = theaterSection.getInt(theaterId + ".second_location.y");
            int regionZ2 = theaterSection.getInt(theaterId + ".second_location.z");
            TheaterRegion theaterRegion = new TheaterRegion(
                    Bukkit.getWorld(regionWorld),
                    regionX1,
                    regionY1,
                    regionZ1,
                    regionX2,
                    regionY2,
                    regionZ2
            );
            WorldGuardUtil.theaterRegions.add(theaterRegion);

            final Theater theater;

            switch (theaterType) {
                case "public":
                    theater = new PublicTheater(cinemaModPlugin, theaterId, theaterName, theaterHidden, screen, theaterRegion);
                    break;
                case "private":
                    theater = new PrivateTheater(cinemaModPlugin, theaterId, theaterName, theaterHidden, screen, theaterRegion);
                    break;
                case "static":
                    String staticUrl = theaterSection.getString(theaterId + ".static.url");
                    /*int staticResWidth;
                    int staticResHeight;
                    if (theaterSection.isSet(theaterId + ".static.res-width") && theaterSection.isSet(theaterId + ".static.res-height")) {
                        staticResWidth = theaterSection.getInt(theaterId + ".static.res-width");
                        staticResHeight = theaterSection.getInt(theaterId + ".static.res-height");
                    } else {
                        staticResWidth = 0;
                        staticResHeight = 0;
                    }*/
                    theater = new StaticTheater(cinemaModPlugin, theaterId, theaterName, theaterHidden, screen, staticUrl, theaterRegion);
                    break;
                case "perms":
                    theater = new PermsTheater(cinemaModPlugin, theaterId, theaterName, theaterHidden, screen, theaterRegion);
                    break;
                default:
                    throw new RuntimeException("Unknown theater type for " + theaterId);
            }

            if (theaterSection.isSet(theaterId + ".preview-screens")) {
                ConfigurationSection previewScreenSection = theaterSection.getConfigurationSection(theaterId + ".preview-screens");
                for (String previewScreenId : previewScreenSection.getKeys(false)) {
                    String previewScreenWorld = previewScreenSection.getString(previewScreenId + ".world");
                    int previewScreenX = previewScreenSection.getInt(previewScreenId + ".x");
                    int previewScreenY = previewScreenSection.getInt(previewScreenId + ".y");
                    int previewScreenZ = previewScreenSection.getInt(previewScreenId + ".z");
                    String previewScreenFacing = previewScreenSection.getString(previewScreenId + ".facing");
                    PreviewScreen previewScreen = new PreviewScreen(previewScreenWorld, previewScreenX, previewScreenY, previewScreenZ, previewScreenFacing);
                    theater.addPreviewScreen(previewScreen);
                }
            }

            theaters.add(theater);
        }

        this.theaters = theaters;
    }

    public void createTheater(Player player,
                              Location firstLocation,
                              Location secondLocation,
                              String name,
                              String type) {
        String theaterName = name;
        boolean theaterHidden = false;
        String theaterType = type;
        World screenWorld = player.getWorld();
        int screenX = firstLocation.getBlockX() + 5;
        int screenY = firstLocation.getBlockY() + 1;
        int screenZ = firstLocation.getBlockZ() + 1;
        String screenFacing = "north";
        float screenWidth = 1f;
        float screenHeight = 1f;
        boolean screenVisible = true;
        boolean screenMuted = false;
        Screen screen = new Screen(screenWorld.getName(), screenX, screenY, screenZ, screenFacing, screenWidth, screenHeight, screenVisible, screenMuted);

        TheaterRegion theaterRegion = new TheaterRegion(
                screenWorld,
                firstLocation.getBlockX(),
                firstLocation.getBlockY(),
                firstLocation.getBlockZ(),
                secondLocation.getBlockX(),
                secondLocation.getBlockY(),
                secondLocation.getBlockZ()
        );
        WorldGuardUtil.theaterRegions.add(theaterRegion);

        final Theater theater;
        if (theaterType.equals("perms")) {
            theater = new PermsTheater(cinemaModPlugin, WorldGuardUtil.getRandomId(), theaterName, theaterHidden, screen, theaterRegion);
            this.theaters.add(theater);
        }
        else if (theaterType.equals("public")) {
            theater = new PublicTheater(cinemaModPlugin, WorldGuardUtil.getRandomId(), theaterName, theaterHidden, screen, theaterRegion);
            this.theaters.add(theater);
        }
    }

    public void createStaticTheater(Player player,
                                    Location firstLocation,
                                    Location secondLocation,
                                    String name,
                                    String type,
                                    String url) {
        String theaterName = name;
        boolean theaterHidden = false;
        String theaterType = type;
        World screenWorld = player.getWorld();
        int screenX = firstLocation.getBlockX() + 5;
        int screenY = firstLocation.getBlockY() + 1;
        int screenZ = firstLocation.getBlockZ() + 1;
        String screenFacing = "north";
        float screenWidth = 1f;
        float screenHeight = 1f;
        boolean screenVisible = true;
        boolean screenMuted = false;
        Screen screen = new Screen(screenWorld.getName(), screenX, screenY, screenZ, screenFacing, screenWidth, screenHeight, screenVisible, screenMuted);

        TheaterRegion theaterRegion = new TheaterRegion(
                screenWorld,
                firstLocation.getBlockX(),
                firstLocation.getBlockY(),
                firstLocation.getBlockZ(),
                secondLocation.getBlockX(),
                secondLocation.getBlockY(),
                secondLocation.getBlockZ()
        );
        WorldGuardUtil.theaterRegions.add(theaterRegion);

        final Theater theater;
        if (theaterType.equals("static")) {
            theater = new StaticTheater(cinemaModPlugin, WorldGuardUtil.getRandomId(), theaterName, theaterHidden, screen, url, theaterRegion);
            this.theaters.add(theater);
        }
    }
}
