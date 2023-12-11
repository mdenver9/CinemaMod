package com.cinemamod.bukkit.util;

import com.cinemamod.bukkit.service.VideoServiceType;
import com.cinemamod.bukkit.theater.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class ChatUtil {

    public static final ChatColor MAIN_COLOR = ChatColor.of("#8F2121");
    public static final ChatColor SECONDARY_COLOR = ChatColor.of("#5e6061");
    private static final String PADDING;

    static {
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            final ChatColor color;
            if (i % 2 == 0) {
                color = MAIN_COLOR;
            } else {
                color = SECONDARY_COLOR;
            }
            paddingBuilder.append(color).append("-");
        }
        PADDING = paddingBuilder.toString();
    }

    public static void sendPaddedMessage(Player player, String... lines) {
        player.sendMessage(PADDING);
        for (String line : lines) {
            if (line != null)
                player.sendMessage(line);
        }
        player.sendMessage(PADDING);
    }

    public static void showPlaying(Player player, Theater theater, boolean showOriginUrl) {
        //boolean privateTheater = theater instanceof PrivateTheater;
        //String playingAt = ChatColor.RESET + " @ " + theater.getName() + " [" + (privateTheater ? "private" : "public") + "]";
        boolean privateTheater = theater instanceof PrivateTheater;
        boolean staticTheater = theater instanceof StaticTheater;
        boolean permsTheater = theater instanceof PermsTheater;
        boolean publicTheater = theater instanceof PublicTheater;
        String type = "?";
        if (privateTheater) type = "private";
        else if (staticTheater) type = "static";
        else if (permsTheater) type = "perms";
        else if (publicTheater) type = "public";
        String playingAt = ChatColor.RESET + " @ " + theater.getName() + " [" + type + "]";

        if (!theater.isPlaying()) {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "Ничего не включено" + playingAt,
                    SECONDARY_COLOR + "Включить видео /video play");
        } else {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "Сейчас включено" + playingAt,
                    SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getTitle(),
                    SECONDARY_COLOR + "Видео включил: " + theater.getPlaying().getRequester().getName(),
                    showOriginUrl ? SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getServiceType().getOriginUrl(theater.getPlaying().getVideoInfo().getId()) : null);

            if (theater.getPlaying().getVideoInfo().getServiceType() == VideoServiceType.TWITCH) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Стрим на твиче может иметь 30-секундный отказ от ответственности до его начала.");
            }
        }
    }

    public static void sendPaddedMessage(String... lines) {
        Bukkit.getServer().getLogger().log(Level.INFO, PADDING);
        for (String line : lines) {
            if (line != null)
                Bukkit.getServer().getLogger().log(Level.INFO, line);
        }
        Bukkit.getServer().getLogger().log(Level.INFO, PADDING);
    }

    public static void showPlaying(Theater theater, boolean showOriginUrl) {
        //boolean privateTheater = theater instanceof PrivateTheater;
        //String playingAt = ChatColor.RESET + " @ " + theater.getName() + " [" + (privateTheater ? "private" : "public") + "]";
        boolean privateTheater = theater instanceof PrivateTheater;
        boolean staticTheater = theater instanceof StaticTheater;
        boolean permsTheater = theater instanceof PermsTheater;
        boolean publicTheater = theater instanceof PublicTheater;
        String type = "?";
        if (privateTheater) type = "private";
        else if (staticTheater) type = "static";
        else if (permsTheater) type = "perms";
        else if (publicTheater) type = "public";
        String playingAt = ChatColor.RESET + " @ " + theater.getName() + " [" + type + "]";

        if (!theater.isPlaying()) {
            sendPaddedMessage(
                    ChatColor.BOLD + "Ничего не включено" + playingAt,
                    SECONDARY_COLOR + "Включить видео /video play");
        } else {
            sendPaddedMessage(
                    ChatColor.BOLD + "Сейчас включено" + playingAt,
                    SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getTitle(),
                    SECONDARY_COLOR + "Видео включил: " + theater.getPlaying().getRequester().getName(),
                    showOriginUrl ? SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getServiceType().getOriginUrl(theater.getPlaying().getVideoInfo().getId()) : null);

            if (theater.getPlaying().getVideoInfo().getServiceType() == VideoServiceType.TWITCH) {
                Bukkit.getServer().getLogger().log(Level.INFO, "Стрим на твиче может иметь 30-секундный отказ от ответственности до его начала.");
            }
        }
    }
}
