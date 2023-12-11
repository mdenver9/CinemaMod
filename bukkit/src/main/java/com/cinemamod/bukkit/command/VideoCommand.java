package com.cinemamod.bukkit.command;

import com.cinemamod.bukkit.CinemaModPlugin;
import com.cinemamod.bukkit.service.VideoURLParser;
import com.cinemamod.bukkit.theater.PermsTheater;
import com.cinemamod.bukkit.theater.StaticTheater;
import com.cinemamod.bukkit.theater.Theater;
import com.cinemamod.bukkit.util.ChatUtil;
import com.cinemamod.bukkit.util.NetworkUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class VideoCommand implements TabExecutor {
    private final CinemaModPlugin cinemaModPlugin;
    private final Set<Player> lock;
    public VideoCommand(CinemaModPlugin cinemaModPlugin) {
        this.cinemaModPlugin = cinemaModPlugin;
        lock = new HashSet<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0 && args[0].equals("play")) {
                return play(player, label, args);
            }
            else if (args.length > 0 && args[0].equals("settings")) {
                NetworkUtil.sendOpenSettingsScreenPacket(cinemaModPlugin, player);
                return true;
            }
            else if (args.length > 0 && args[0].equals("info")) {
                return info(player);
            }
            else if (args.length > 0 && args[0].equals("download_mod")) {
                return download(player);
            }
            else if (args.length > 0 && args[0].equals("skip")) {
                return skip(player);
            }
            else if (args.length > 0 && args[0].equals("help")) {
                return help(player);
            }

            else if (args.length > 0 && args[0].equals("op_lock")) {
                return opLock(player);
            }
            else if (args.length > 0 && args[0].equals("op_skip")) {
                return opSkip(player);
            }

            else if (args.length > 0 && args[0].equals("perms_lock")) {
                return permsLock(player);
            }
            else if (args.length > 0 && args[0].equals("perms_skip")) {
                return permsSkip(player);
            }
        }
        else if (sender instanceof ConsoleCommandSender) {
            if (args.length > 0 && args[0].equals("console_lock")) {
                return consoleLock(args);
            }
            else if (args.length > 0 && args[0].equals("console_skip")) {
                return consoleSkip(args);
            }
            else if (args.length > 0 && args[0].equals("console_play")) {
                return consolePlay(label, args);
            }
            else if (args.length > 0 && args[0].equals("console_help")) {
                return consoleHelp();
            }
            else if (args.length > 0 && args[0].equals("console_info")) {
                return consoleInfo(args);
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            if (sender instanceof Player player) {
                arguments.add("play");
                arguments.add("settings");
                arguments.add("info");
                arguments.add("download_mod");
                arguments.add("skip");
                arguments.add("help");
                if (player.isOp()) {
                    arguments.add("op_lock");
                    arguments.add("op_skip");
                }
                else if (player.hasPermission("cinemamod.perms")) {
                    arguments.add("perms_lock");
                    arguments.add("perms_skip");
                }
            }
            else if (sender instanceof ConsoleCommandSender) {
                arguments.add("console_lock");
                arguments.add("console_skip");
                arguments.add("console_play");
                arguments.add("console_info");
                arguments.add("console_help");
            }
            return arguments;
        }
        else if (args.length == 2) {
            if (args[0].equals("play")) {
                List<String> arguments = new ArrayList<>();
                arguments.add("<url>");
                return arguments;
            }
        }
        return null;
    }

    private boolean play(Player player, String label, String args[]) {
        if (lock.contains(player)) {
            player.sendMessage(ChatColor.RED + "Подождите, чтобы использовать эту команду снова.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Ссылка неправильная. /" + label + " <url>");
            player.sendMessage(ChatColor.RED + "Например: /" + label + " play https://www.youtube.com/watch?v=m_QiIhyQZqA");
            return true;
        }

        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        if (theater instanceof PermsTheater) {
            if (!player.hasPermission("cinemamod.perms") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "В этой области у вас нет прав.");
                return true;
            }
        }

        String url = args[1];
        VideoURLParser parser = new VideoURLParser(cinemaModPlugin, url);

        parser.parse(player);

        if (!parser.found()) {
            player.sendMessage(ChatColor.RED + "Эта ссылка или видео не поддерживается.");
            return true;
        }

        if (!player.hasPermission(parser.getInfoFetcher().getPermission())) {
            player.sendMessage(ChatColor.RED + "У Вас нет прав.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Получение информации...");

        lock.add(player);

        parser.getInfoFetcher().fetch().thenAccept(videoInfo -> {
            lock.remove(player);

            if (!player.isOnline()) return;

            if (!theater.isViewer(player)) {
                player.sendMessage(ChatColor.RED + "Видео которое вы включили не было добавлено в очередь, так как вы покинули область.");
                return;
            }

            if (videoInfo == null) {
                player.sendMessage(ChatColor.RED + "Невозможно получить информацию о видео.");
                return;
            }

            theater.getVideoQueue().processPlayerRequest(videoInfo, player);
        });

        return true;
    }

    private boolean info(Player player) {
        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        ChatUtil.showPlaying(player, theater, true);
        return true;
    }

    private boolean download(Player player) {
        player.sendMessage(ChatColor.GOLD + "Требуется: Fabric 1.20.1 + fabric api");
        player.sendMessage(ChatColor.DARK_GRAY + "-------------------------------------");
        player.sendMessage(ChatColor.GOLD + "Для Windows(Intel и AMD): https://github.com/CinemaMod/CinemaMod/releases/download/1.0.6-1.20.1/cinemamod-windows_amd64-1.0.6-1.20.1.jar");
        player.sendMessage(ChatColor.GOLD + "Для Windows(arm): https://github.com/CinemaMod/CinemaMod/releases/download/1.0.6-1.20.1/cinemamod-windows_arm64-1.0.6-1.20.1.jar");
        player.sendMessage(ChatColor.GOLD + "Для Linux(Intel и AMD): https://github.com/CinemaMod/CinemaMod/releases/download/1.0.6-1.20.1/cinemamod-linux_amd64-1.0.6-1.20.1.jar");
        player.sendMessage(ChatColor.GOLD + "Для Linux(arm): https://github.com/CinemaMod/CinemaMod/releases/download/1.0.6-1.20.1/cinemamod-linux_arm64-1.0.6-1.20.1.jar");
        player.sendMessage(ChatColor.DARK_GRAY + "-------------------------------------");
        return true;
    }

    private boolean skip(Player player) {
        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        if (theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Здесь может быть запущено только одно видео.");
            return true;
        }

        if (theater instanceof PermsTheater) {
            if (!player.hasPermission("cinemamod.perms") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "В этой области у вас нет прав.");
                return true;
            }
        }

        if (!theater.isPlaying()) {
            player.sendMessage(ChatColor.RED + "Нет видео для скипа.");
        } else if (!theater.addVoteSkip(player)) {
            player.sendMessage(ChatColor.RED + "Вы уже проголосовали за скип видео.");
        }
        return true;
    }

    private boolean help(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
        player.sendMessage(ChatColor.GRAY + "   play - добавить видео в очередь");
        player.sendMessage(ChatColor.GRAY + "   settings - настройки");
        player.sendMessage(ChatColor.GRAY + "   info - что сейчас включено");
        player.sendMessage(ChatColor.GRAY + "   skip - проголосовать за скип");
        if (player.isOp()) {
            player.sendMessage(ChatColor.DARK_RED + "   op_lock - видосы нельзя будет добавлять в очередь");
            player.sendMessage(ChatColor.DARK_RED + "   op_skip - скип текущего видео");
        }
        else if (player.hasPermission("cinemamod.perms")) {
            player.sendMessage(ChatColor.DARK_RED + "   perms_lock - видосы нельзя будет добавлять в очередь");
            player.sendMessage(ChatColor.DARK_RED + "   perms_skip - скип текущего видео");
        }
        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
        return true;
    }

    private boolean opLock(Player player) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "У Вас нет прав.");
            return true;
        }

        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        boolean wasLocked = theater.getVideoQueue().isLocked();
        theater.getVideoQueue().setLocked(!wasLocked);
        if (wasLocked) {
            player.sendMessage(ChatColor.GREEN + "Видео-очередь разблокирована.");
        } else {
            player.sendMessage(ChatColor.RED + "Видео-очередь заблокирована.");
        }
        return true;
    }

    private boolean opSkip(Player player) {
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "У Вас нет прав.");
            return true;
        }

        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        if (theater.isPlaying()) {
            theater.forceSkip();
            player.sendMessage(ChatColor.GREEN + "Видео скипнуто высшими силами.");
        } else {
            player.sendMessage(ChatColor.RED + "Никаких видео не включено.");
        }
        return true;
    }

    private boolean permsLock(Player player) {
        if (!player.hasPermission("cinemamod.perms")) {
            player.sendMessage(ChatColor.RED + "У Вас нет прав.");
            return true;
        }

        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        boolean wasLocked = theater.getVideoQueue().isLocked();
        theater.getVideoQueue().setLocked(!wasLocked);
        if (wasLocked) {
            player.sendMessage(ChatColor.GREEN + "Видео-очередь разблокирована.");
        } else {
            player.sendMessage(ChatColor.RED + "Видео-очередь заблокирована.");
        }
        return true;
    }

    private boolean permsSkip(Player player) {
        if (!player.hasPermission("cinemamod.perms")) {
            player.sendMessage(ChatColor.RED + "У Вас нет прав.");
            return true;
        }

        Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
            return true;
        }

        if (theater.isPlaying()) {
            theater.forceSkip();
            player.sendMessage(ChatColor.GREEN + "Видео скипнуто высшими силами.");
        } else {
            player.sendMessage(ChatColor.RED + "Никаких видео не включено.");
        }
        return true;
    }

    private boolean consoleLock(String[] args) {
        try {
            Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheaterByName(args[1]);
            if (theater == null || theater instanceof StaticTheater) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Театр не найден или он статический!");
                return true;
            }

            boolean wasLocked = theater.getVideoQueue().isLocked();
            theater.getVideoQueue().setLocked(!wasLocked);
            if (wasLocked) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Видео-очередь разблокирована.");
            } else {
                cinemaModPlugin.getLogger().log(Level.INFO, "Видео-очередь заблокирована.");
            }
        }
        catch (Exception ignore) {
            cinemaModPlugin.getLogger().log(Level.WARNING, "Ошибка. Возможно вы забыли указать название теарта.");
        }
        return true;
    }

    private boolean consoleSkip(String[] args) {
        try {
            Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheaterByName(args[1]);
            if (theater == null || theater instanceof StaticTheater) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Театр не найден или он статический!");
                return true;
            }

            if (theater.isPlaying()) {
                theater.forceSkip();
                cinemaModPlugin.getLogger().log(Level.INFO, "Скипнуто!");
            } else {
                cinemaModPlugin.getLogger().log(Level.INFO, "Никакие видео не включены!");
            }
        }
        catch (Exception ignore) {
            cinemaModPlugin.getLogger().log(Level.WARNING, "Ошибка. Возможно вы забыли указать название теарта.");
        }
        return true;
    }

    private boolean consolePlay(String label, String args[]) {
        try {
            if (args.length < 2) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Ссылка неправильная. /" + label + " <url>");
                cinemaModPlugin.getLogger().log(Level.INFO, "Например: /" + label + " play https://www.youtube.com/watch?v=m_QiIhyQZqA");
                return true;
            }

            Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheaterByName(args[2]);
            if (theater == null || theater instanceof StaticTheater) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Театр не найден или он статический!");
                return true;
            }

            String url = args[1];
            VideoURLParser parser = new VideoURLParser(cinemaModPlugin, url);

            parser.parse(null);

            if (!parser.found()) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Эта ссылка или видео не поддерживается.");
                return true;
            }

            cinemaModPlugin.getLogger().log(Level.INFO, "Получение информации...");

            parser.getInfoFetcher().fetch().thenAccept(videoInfo -> {
                if (videoInfo == null) {
                    cinemaModPlugin.getLogger().log(Level.INFO, "Невозможно получить информацию о видео.");
                    return;
                }

                //TODO
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    theater.getVideoQueue().processPlayerRequestByServer(videoInfo, Bukkit.getOnlinePlayers().iterator().next());
                }
            });
        } catch (Exception ignore) {
                cinemaModPlugin.getLogger().log(Level.WARNING, "Ошибка. Возможно вы забыли указать название теарта.");
        }
        return true;
    }

    private boolean consoleHelp() {
        cinemaModPlugin.getLogger().log(Level.INFO, "--- Помощь ---");
        cinemaModPlugin.getLogger().log(Level.INFO, "Синтаксис команд такойже как для игрока, только в конце каждой команды, указывается название театра!");
        cinemaModPlugin.getLogger().log(Level.INFO, "   console_info - что сейчас включено");
        cinemaModPlugin.getLogger().log(Level.INFO, "   console_play - добавить видео в очередь");
        cinemaModPlugin.getLogger().log(Level.INFO, "   console_lock - видосы нельзя будет добавлять в очередь");
        cinemaModPlugin.getLogger().log(Level.INFO, "   console_skip - скип текущего видео");
        cinemaModPlugin.getLogger().log(Level.INFO, "--- Помощь ---");
        return true;
    }

    private boolean consoleInfo(String args[]) {
        try {
            Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheaterByName(args[1]);
            if (theater == null || theater instanceof StaticTheater) {
                cinemaModPlugin.getLogger().log(Level.INFO, "Театр не найден или он статический!");
                return true;
            }

            ChatUtil.showPlaying(theater, true);
        } catch (Exception ignore) {
            cinemaModPlugin.getLogger().log(Level.WARNING, "Ошибка. Возможно вы забыли указать название теарта.");
        }
        return true;
    }
}
