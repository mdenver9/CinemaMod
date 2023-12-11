package com.cinemamod.bukkit.command;

import com.cinemamod.bukkit.CinemaModPlugin;
import com.cinemamod.bukkit.theater.Theater;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CinemaCommand implements TabExecutor {
    private final CinemaModPlugin cinemaModPlugin;
    public CinemaCommand(CinemaModPlugin cinemaModPlugin) {
        this.cinemaModPlugin = cinemaModPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (args.length == 1) {
                if (args[0].equals("help")) {
                    player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
                    player.sendMessage(ChatColor.GRAY + "   create - создать область театра, вместе с его типом, его изменить потом нельзя будет!");
                    player.sendMessage(ChatColor.GRAY + "   screen - настроить экран в театре, находясь в области.");
                    player.sendMessage(ChatColor.GRAY + "   save - сохранить натройки театров в конфиг.");
                    player.sendMessage(ChatColor.GRAY + "   delete - удалить театр, находясь в области");
                    player.sendMessage(ChatColor.GRAY + "   /cinema help create|screen - подробная помощь.");
                    player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
                    return true;
                }
                else if (args[0].equals("save")) {
                    cinemaModPlugin.saveNewConfig();
                    player.sendMessage(ChatColor.GREEN + "Настройки всех театров сохранены в конфиг!");
                    return true;
                }
                else if (args[0].equals("delete")) {
                    Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
                    if (theater == null) {
                        player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
                        return true;
                    }

                    cinemaModPlugin.getTheaterManager().getTheaters().removeIf(x -> x.getId().equals(theater.getId()));

                    player.sendMessage(ChatColor.GREEN + "Театр удален. Сохраните изменения, чтобы он не создавался вновь.");
                    return true;
                }
            }
            else if (args.length == 2) {
                if (args[0].equals("help")) {
                    if (args[1].equals("create")) {
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь create ---");
                        player.sendMessage(ChatColor.GRAY + "   синтаксис команды - /cinema create x1 y1 z1 x2 y2 z2 имя тип");
                        player.sendMessage(ChatColor.GRAY + "   x,y,z координаты области театра");
                        player.sendMessage(ChatColor.GRAY + "   первая и вторая точка, которые образуют трехмерное поле");
                        player.sendMessage(ChatColor.GRAY + "   имя - название кинотеатра");
                        player.sendMessage(ChatColor.GRAY + "   тип - тип кинотеатра, подробнее описано в конфиге");
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
                        return true;
                    }
                    else if (args[1].equals("screen")) {
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь screen ---");
                        player.sendMessage(ChatColor.GRAY + "   синтаксис команды - /cinema screen x y z face ширина высота");
                        player.sendMessage(ChatColor.GRAY + "   точка блока экрана, от нее будет растягиваться ширина и высота");
                        player.sendMessage(ChatColor.GRAY + "   точка блока экрана, визуально уничтожит блок если он там установлен");
                        player.sendMessage(ChatColor.GRAY + "   выбирайте левую верхнюю точку для удобства");
                        player.sendMessage(ChatColor.GRAY + "   face в какую сторону будет смотреть экран (например: игрок смотрит на NORTH, значит экран должен смотреть на SOUTH");
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
                        return true;
                    }
                    else if (args[1].equals("save")) {
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь save ---");
                        player.sendMessage(ChatColor.GRAY + "   все изменения которые вы делали сохраняет в конфиг плагина");
                        player.sendMessage(ChatColor.GRAY + "   (создание/удаление театров)");
                        player.sendMessage(ChatColor.GRAY + "   (изменения скрина театров)");
                        player.sendMessage(ChatColor.GRAY + "   если не сохранять изменения, то после рестарта она пропадут");
                        player.sendMessage(ChatColor.DARK_GRAY + "--- Помощь ---");
                        return true;
                    }
                }
            }
            else if (args.length == 9) {
                if (args[0].equals("create") && !args[8].equals("static")) {
                    try {
                        cinemaModPlugin.getTheaterManager().createTheater(player,
                                new Location(player.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])),
                                new Location(player.getWorld(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6])),
                                args[7],
                                args[8]);

                        player.sendMessage(ChatColor.GREEN + "Новый театр временно создан.");
                        return true;
                    } catch (Exception ignore) {
                        player.sendMessage(ChatColor.RED + "Ошибка...");
                        return false;
                    }
                }
            }
            else if (args.length == 10) {
                if (args[0].equals("create") && args[8].equals("static")) {
                    try {
                        cinemaModPlugin.getTheaterManager().createStaticTheater(player,
                                new Location(player.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])),
                                new Location(player.getWorld(), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6])),
                                args[7],
                                args[8],
                                args[9]);

                        player.sendMessage(ChatColor.GREEN + "Новый театр временно создан.");
                        return true;
                    } catch (Exception ignore) {
                        player.sendMessage(ChatColor.RED + "Ошибка...");
                        return false;
                    }
                }
            }
            else if (args.length == 7) {
                if (args[0].equals("screen")) {
                    Theater theater = cinemaModPlugin.getTheaterManager().getCurrentTheater(player);
                    if (theater == null) {
                        player.sendMessage(ChatColor.RED + "Вы должны быть в пределах видео, чтобы использовать команду.");
                        return true;
                    }

                    try {
                        theater.getScreen().fromCommand(
                                Integer.parseInt(args[1]),
                                Integer.parseInt(args[2]),
                                Integer.parseInt(args[3]),
                                args[4],
                                Float.parseFloat(args[5]),
                                Float.parseFloat(args[6])
                        );
                        player.sendMessage(ChatColor.GREEN + "Новые настройки экрана временно применены.");
                        player.sendMessage(ChatColor.GREEN + "Перезайдите на сервер, чтобы увидеть изменения.");

                        return true;
                    } catch (Exception ignore) {
                        player.sendMessage(ChatColor.RED + "Ошибка...");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            arguments.add("help");
            arguments.add("create");
            arguments.add("screen");
            arguments.add("save");
            arguments.add("delete");
            return arguments;
        }
        else if (args.length == 2) { //cinema create
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getX()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getX()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("help")) { //cinema help
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();
                    arguments.add("create");
                    arguments.add("screen");
                    arguments.add("save");
                    return arguments;
                }
            }
        }
        else if (args.length == 3) { //cinema create x1
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getY()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen x1
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getY()));
                    }

                    return arguments;
                }
            }
        }
        else if (args.length == 4) { //cinema create x1 y1
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getZ()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen x1 y1
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getZ()));
                    }

                    return arguments;
                }
            }
        }
        else if (args.length == 5) { //cinema create x1 y1 z1
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getX()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen x1 y1 z1
                List<String> arguments = new ArrayList<>();
                arguments.add("EAST");
                arguments.add("NORTH");
                arguments.add("SOUTH");
                arguments.add("WEST");
                return arguments;
            }
        }
        else if (args.length == 6) { //cinema create x1 y1 z1 x2
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getY()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen x1 y1 z1 face
                List<String> arguments = new ArrayList<>();
                arguments.add("<width>");
                return arguments;
            }
        }
        else if (args.length == 7) { //cinema create x1 y1 z1 x2 y2
            if (args[0].equals("create")) {
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    List<String> arguments = new ArrayList<>();

                    Block block = player.getTargetBlockExact(10);
                    if (block != null && !block.getType().isAir()) {
                        arguments.add(String.valueOf(block.getZ()));
                    }

                    return arguments;
                }
            }
            else if (args[0].equals("screen")) { //cinema screen x1 y1 z1 face width
                List<String> arguments = new ArrayList<>();
                arguments.add("<height>");
                return arguments;
            }
        }
        else if (args.length == 8) { //cinema create x1 y1 z1 x2 y2 z2
            if (args[0].equals("create")) {
                List<String> arguments = new ArrayList<>();
                arguments.add("name");
                return arguments;
            }
        }
        else if (args.length == 9) { //cinema create x1 y1 z1 x2 y2 z2 name
            if (args[0].equals("create")) {
                List<String> arguments = new ArrayList<>();
                arguments.add("public");
                arguments.add("static");
                arguments.add("perms");
                return arguments;
            }
        }
        else if (args.length == 10) { //cinema create x1 y1 z1 x2 y2 z2 name static
            if (args[0].equals("create") && args[8].equals("static")) {
                List<String> arguments = new ArrayList<>();
                arguments.add("<url>");
                return arguments;
            }
        }

        return null;
    }
}
