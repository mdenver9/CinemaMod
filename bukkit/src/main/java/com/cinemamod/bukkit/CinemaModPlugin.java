package com.cinemamod.bukkit;

import com.cinemamod.bukkit.command.CinemaCommand;
import com.cinemamod.bukkit.command.HistoryCommand;
import com.cinemamod.bukkit.command.VideoCommand;
import com.cinemamod.bukkit.command.VolumeCommand;
import com.cinemamod.bukkit.command.theater.*;
import com.cinemamod.bukkit.listener.PlayerJoinQuitListener;
import com.cinemamod.bukkit.listener.PlayerTheaterListener;
import com.cinemamod.bukkit.listener.PlayerVideoTimelineListener;
import com.cinemamod.bukkit.player.PlayerDataManager;
import com.cinemamod.bukkit.service.infofetcher.FileVideoInfoFetcher;
import com.cinemamod.bukkit.storage.VideoStorage;
import com.cinemamod.bukkit.storage.sql.MySQLDriver;
import com.cinemamod.bukkit.storage.sql.SQLDriver;
import com.cinemamod.bukkit.storage.sql.SQLiteDriver;
import com.cinemamod.bukkit.storage.sql.video.SQLVideoStorage;
import com.cinemamod.bukkit.task.PlayerListUpdateTask;
import com.cinemamod.bukkit.theater.*;
import com.cinemamod.bukkit.util.NetworkUtil;
import com.cinemamod.bukkit.util.ProtocolLibUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CinemaModPlugin extends JavaPlugin {

    private CinemaModConfig cinemaModConfig;
    private TheaterManager theaterManager;
    private VideoStorage videoStorage;
    private PlayerDataManager playerDataManager;

    public CinemaModConfig getCinemaModConfig() {
        return cinemaModConfig;
    }

    public TheaterManager getTheaterManager() {
        return theaterManager;
    }

    public VideoStorage getVideoStorage() {
        return videoStorage;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        cinemaModConfig = new CinemaModConfig();
        cinemaModConfig.youtubeDataApiKey = getConfig().getString("youtube-data-api-key");
        cinemaModConfig.enableTabTheaterList = getConfig().getBoolean("enable-tab-theater-list");
        cinemaModConfig.useMysql = getConfig().getBoolean("storage.mysql.use");
        cinemaModConfig.mysqlHost = getConfig().getString("storage.mysql.host");
        cinemaModConfig.mysqlPort = getConfig().getInt("storage.mysql.port");
        cinemaModConfig.mysqlDatabase = getConfig().getString("storage.mysql.database");
        cinemaModConfig.mysqlUsername = getConfig().getString("storage.mysql.username");
        cinemaModConfig.mysqlPassword = getConfig().getString("storage.mysql.password");

        cinemaModConfig.isDownVote = getConfig().getBoolean("down_vote_enable");
        cinemaModConfig.isBossBarsShow = getConfig().getBoolean("boss_bars_show_enable");

        cinemaModConfig.autogenCubicRegions = getConfig().getBoolean("autogenCubicRegions");

        if (cinemaModConfig.youtubeDataApiKey.length() != 39) {
            getLogger().warning("Invalid YouTube Data API V3 key. YouTube videos will not be able to be requested.");
        }

        theaterManager = new TheaterManager(this);
        theaterManager.loadFromConfig(getConfig().getConfigurationSection("theaters"));

        SQLDriver sqlDriver = null;

        if (cinemaModConfig.useMysql) {
            sqlDriver = new MySQLDriver(cinemaModConfig);
        } else if (cinemaModConfig.useSqlite) {
            File dbFile = new File(getDataFolder(), "video_storage.db");
            try {
                sqlDriver = new SQLiteDriver(dbFile);
            } catch (IOException ignored) {
                getLogger().warning("Unable to create or load database file");
            }
        }

        if (sqlDriver == null) {
            getLogger().warning("Could not initialize video storage");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            videoStorage = new SQLVideoStorage(sqlDriver);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        playerDataManager = new PlayerDataManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTheaterListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVideoTimelineListener(this), this);
        getServer().getScheduler().runTaskTimer(this, () -> theaterManager.tickTheaters(), 20L, 20L);

        if (cinemaModConfig.enableTabTheaterList) {
            getServer().getScheduler().runTaskTimer(this, new PlayerListUpdateTask(this), 20L, 20L);
        }

        getCommand("video").setExecutor(new VideoCommand(this));
        getCommand("cinema").setExecutor(new CinemaCommand(this));

        //getCommand("request").setExecutor(new RequestCommand(this));
        //getCommand("forceskip").setExecutor(new ForceSkipCommand(this));
        //getCommand("voteskip").setExecutor(new VoteSkipCommand(this));
        //getCommand("lockqueue").setExecutor(new LockQueueCommand(this));
        //getCommand("volume").setExecutor(new VolumeCommand(this));
        //getCommand("playing").setExecutor(new PlayingCommand(this));
        //getCommand("history").setExecutor(new HistoryCommand(this));

        NetworkUtil.registerChannels(this);
        ProtocolLibUtil.registerSoundPacketListener(this);
    }

    public void saveNewConfig() {
        ConfigurationSection theaterSection = getConfig().getConfigurationSection("theaters");

        for (Theater theater: getTheaterManager().getTheaters()) {
            boolean hasInConfig = false;

            if (theaterSection != null) {
                for (String theaterId : theaterSection.getKeys(false)) {
                    if (!theaterId.equals(theater.getId())) continue;
                    if (!hasInConfig) {
                        String type = "perms";
                        if (theater instanceof StaticTheater) {
                            type = "static";
                        } else if (theater instanceof PublicTheater) {
                            type = "public";
                        } else if (theater instanceof PermsTheater) {
                            type = "perms";
                        }

                        theaterSection.set(theaterId + ".type", type);
                        theaterSection.set(theaterId + ".screen.world", theater.getScreen().getWorld());
                        theaterSection.set(theaterId + ".screen.x", theater.getScreen().getX());
                        theaterSection.set(theaterId + ".screen.y", theater.getScreen().getY());
                        theaterSection.set(theaterId + ".screen.z", theater.getScreen().getZ());
                        theaterSection.set(theaterId + ".screen.facing", theater.getScreen().getFacing());
                        theaterSection.set(theaterId + ".screen.width", theater.getScreen().getWidth());
                        theaterSection.set(theaterId + ".screen.height", theater.getScreen().getHeight());
                        theaterSection.set(theaterId + ".screen.visible", theater.getScreen().isVisible());
                        theaterSection.set(theaterId + ".screen.muted", theater.getScreen().isMuted());

                        theaterSection.set(theaterId + ".first_location.world", theater.getRegions().get(0).getFirstLocation().getWorld().getName());
                        theaterSection.set(theaterId + ".first_location.x", theater.getRegions().get(0).getFirstLocation().getBlockX());
                        theaterSection.set(theaterId + ".first_location.y", theater.getRegions().get(0).getFirstLocation().getBlockY());
                        theaterSection.set(theaterId + ".first_location.z", theater.getRegions().get(0).getFirstLocation().getBlockZ());
                        theaterSection.set(theaterId + ".second_location.x", theater.getRegions().get(0).getSecondLocation().getBlockX());
                        theaterSection.set(theaterId + ".second_location.y", theater.getRegions().get(0).getSecondLocation().getBlockY());
                        theaterSection.set(theaterId + ".second_location.z", theater.getRegions().get(0).getSecondLocation().getBlockZ());
                        hasInConfig = true;
                        break;
                    }
                }
            }
            else {
                theaterSection = getConfig().createSection("theaters");
            }

            if (!hasInConfig) {
                String theaterId = theater.getId();
                theaterSection.createSection(theaterId);

                String type = "perms";
                if (theater instanceof StaticTheater) {
                    type = "static";
                }
                else if (theater instanceof PublicTheater) {
                    type = "public";
                }
                else if (theater instanceof PermsTheater) {
                    type = "perms";
                }
                theaterSection.createSection(theaterId + ".name");
                theaterSection.createSection(theaterId + ".hidden");
                theaterSection.createSection(theaterId + ".type");
                theaterSection.createSection(theaterId + ".screen.world");
                theaterSection.createSection(theaterId + ".screen.x");
                theaterSection.createSection(theaterId + ".screen.y");
                theaterSection.createSection(theaterId + ".screen.z");
                theaterSection.createSection(theaterId + ".screen.facing");
                theaterSection.createSection(theaterId + ".screen.width");
                theaterSection.createSection(theaterId + ".screen.height");
                theaterSection.createSection(theaterId + ".screen.visible");
                theaterSection.createSection(theaterId + ".screen.muted");

                theaterSection.createSection(theaterId + ".first_location.world");
                theaterSection.createSection(theaterId + ".first_location.x");
                theaterSection.createSection(theaterId + ".first_location.y");
                theaterSection.createSection(theaterId + ".first_location.z");
                theaterSection.createSection(theaterId + ".second_location.x");
                theaterSection.createSection(theaterId + ".second_location.y");
                theaterSection.createSection(theaterId + ".second_location.z");



                theaterSection.set(theaterId + ".name", theater.getName());
                theaterSection.set(theaterId + ".hidden", theater.isHidden());
                theaterSection.set(theaterId + ".type", type);
                theaterSection.set(theaterId + ".screen.world", theater.getScreen().getWorld());
                theaterSection.set(theaterId + ".screen.x", theater.getScreen().getX());
                theaterSection.set(theaterId + ".screen.y", theater.getScreen().getY());
                theaterSection.set(theaterId + ".screen.z", theater.getScreen().getZ());
                theaterSection.set(theaterId + ".screen.facing", theater.getScreen().getFacing());
                theaterSection.set(theaterId + ".screen.width", theater.getScreen().getWidth());
                theaterSection.set(theaterId + ".screen.height", theater.getScreen().getHeight());
                theaterSection.set(theaterId + ".screen.visible", theater.getScreen().isVisible());
                theaterSection.set(theaterId + ".screen.muted", theater.getScreen().isMuted());
                if (type.equals("static")) {
                    theaterSection.set(theaterId + ".static.url", ((StaticTheater)theater).getUrl());
                }

                theaterSection.set(theaterId + ".first_location.world", theater.getRegions().get(0).getFirstLocation().getWorld().getName());
                theaterSection.set(theaterId + ".first_location.x", theater.getRegions().get(0).getFirstLocation().getBlockX());
                theaterSection.set(theaterId + ".first_location.y", theater.getRegions().get(0).getFirstLocation().getBlockY());
                theaterSection.set(theaterId + ".first_location.z", theater.getRegions().get(0).getFirstLocation().getBlockZ());
                theaterSection.set(theaterId + ".second_location.x", theater.getRegions().get(0).getSecondLocation().getBlockX());
                theaterSection.set(theaterId + ".second_location.y", theater.getRegions().get(0).getSecondLocation().getBlockY());
                theaterSection.set(theaterId + ".second_location.z", theater.getRegions().get(0).getSecondLocation().getBlockZ());
            }
        }


        //delete theaters
        for (String theaterId : theaterSection.getKeys(false)) {
            if (getTheaterManager().getTheaters().stream().noneMatch(x -> x.getId().equals(theaterId))) {
                theaterSection.set(theaterId, null);
            }
        }

        saveConfig();
    }
}
