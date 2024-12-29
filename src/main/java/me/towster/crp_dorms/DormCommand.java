package me.towster.crp_dorms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class DormCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args[0].equals("saveData")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions"); 
            }
            
            CRP_Dorms.saveData();
            sender.sendMessage("Saved data");
        } else if (args[0].equals("allocate")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (args.length != 3) {
                sender.sendMessage("Not enough arguments");
                return false;
            }

            Player[] playerList = (Player[]) Bukkit.getOnlinePlayers().toArray();
            Player selectedPlayer = null;
            for (Player player : playerList) {
                if (args[0].equals(player.getName())) {
                    selectedPlayer = player;
                }
            }
            if (selectedPlayer == null) {
                sender.sendMessage("That player does not exist");
                return false;
            }

            int dormIndex = Integer.parseInt(args[1]);
            if (dormIndex >= CRP_Dorms.dormList.size()) {
                sender.sendMessage("That dorm index is out of scope");
                return false;
            }

            if (CRP_Dorms.dormPlayerAllocation.get(selectedPlayer.getUniqueId()) == dormIndex) {
                CRP_Dorms.dormPlayerAllocation.remove(selectedPlayer.getUniqueId());

                sender.sendMessage(ChatColor.BLUE + "You kicked " + selectedPlayer.getName() + " out of dorm " + dormIndex);
            } else {

                CRP_Dorms.dormPlayerAllocation.put(selectedPlayer.getUniqueId(), dormIndex);

                sender.sendMessage(ChatColor.GREEN + selectedPlayer.getName() + " Is now the owner of dorm " + dormIndex);
            }

            CRP_Dorms.dormPlayerAllocation.put(selectedPlayer.getUniqueId(), dormIndex);
        } else if (args[0].equals("allocateSelf")) {
            if (!sender.hasPermission("dorm.user")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "A player must run this command. Not you :(");
                return false;
            }
            Player player = (Player) sender;

            if (CRP_Dorms.dormPlayerAllocation.get(player.getUniqueId()) == Integer.parseInt(args[1])) {
                CRP_Dorms.dormPlayerAllocation.remove(player.getUniqueId());

                sender.sendMessage(ChatColor.BLUE + "You kicked yourself out of the dorm");
            } else {

                CRP_Dorms.dormPlayerAllocation.put(player.getUniqueId(), Integer.parseInt(args[1]));

                sender.sendMessage(ChatColor.GREEN + "You now the owner to this dorm");
            }
        } else if (args[0].equals("edit")) {
            if (!sender.hasPermission("dorm.user")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            CRP_Dorms.dormPlayerAllocation.forEach((uuid, dormIndex) -> {
                Bukkit.getOnlinePlayers().forEach((player) -> {
                    if (player.getUniqueId().equals(uuid) && CRP_Dorms.dormList.get(dormIndex).isPlayerInDorm(player)) {
                        player.setGameMode(GameMode.CREATIVE);
                        player.sendMessage(ChatColor.GREEN + "You are now editing your dorm");
                        CRP_Dorms.editingPlayers.add(player);
                    }
                });

            });
        }
        else if (args[0].equals("save")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "A player must run this command. Not you :(");
                return false;
            }
            Player player = (Player) sender;
            CRP_Dorms.dormList.add(
                    CRP_Dorms.highlightedDorms.get(player.getUniqueId())
            );

            sender.sendMessage("Saved Dorm");
        } else if (args[0].equals("createDormPlacement")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "A player must run this command. Not you :(");
                return false;
            }
            if (args.length != 6) {
                sender.sendMessage("Not enough/Too many arguments");
                return false;
            }

            Player player = (Player) sender;
            float playerYaw = player.getYaw();
            DIRECTION direction = DIRECTION.NORTH;
            if (playerYaw < -135) {
                direction = DIRECTION.NORTH;
            } else if (playerYaw < -45) {
                direction = DIRECTION.EAST;
            } else if (playerYaw < 45) {
                direction = DIRECTION.SOUTH;
            } else if (playerYaw < 135) {
                direction = DIRECTION.WEST;
            }

            CRP_Dorms.highlightedDorms.put(player.getUniqueId(),
                    new Dorm(player.getWorld(),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]),
                            Integer.parseInt(args[5]),
                            player.getLocation().toBlockLocation().toVector(),
                            direction
                    )
            );

        } else if (args[0].equals("list")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "A player must run this command. Not you :(");
                return false;
            }
            Player player = (Player) sender;

            sender.sendMessage("From Nearest to furthest:");
            List<SimpleEntry<Integer, Vector>> dormCenters = new ArrayList<>();

            int inWorldCount = 0;
            for (int i = 0; i < CRP_Dorms.dormList.size(); i++) {
                Dorm dorm = CRP_Dorms.dormList.get(i);

                if (dorm.world.equals(player.getWorld())) {
                    dormCenters.add(new SimpleEntry<>(i, dorm.getCenter()));
                    inWorldCount ++;
                }
            }

            Vector playerVector = player.getLocation().toVector();
            dormCenters.sort((a, b) -> (int) (a.getValue().distanceSquared(playerVector) - b.getValue().distanceSquared(playerVector)));

            player.sendMessage("There were " + inWorldCount + " dorms in this world");
            for (SimpleEntry<Integer, Vector> end : dormCenters) {
                player.sendMessage("Index: " + end.getKey() + " Center: " + end.getValue().getX() + ", " + end.getValue().getY() + ", " + end.getValue().getZ());
            }

        } else if (args[0].equals("show")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            if (args.length != 2) {
                sender.sendMessage("Not enough/Too many arguments");
                return false;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("A player must run this command not you :(");
                return false;
            }

            Player player = (Player) sender;

            CRP_Dorms.highlightedDorms.put(player.getUniqueId(), CRP_Dorms.dormList.get(Integer.parseInt(args[1])));

            sender.sendMessage("Showing dorm with index: " + Integer.parseInt(args[1]));

        } else if (args[0].equals("stopShowing")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "A player must run this command. Not you :(");
                return false;
            }

            Player player = (Player) sender;

            CRP_Dorms.highlightedDorms.remove(player.getUniqueId());

        } else if (args[0].equals("playerList")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            for (int index = 0; index < CRP_Dorms.dormPlayerAllocation.size(); index ++) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getUniqueId().equals(CRP_Dorms.dormPlayerAllocation.get(index))) {
                        sender.sendMessage(player.getName() + " has the dorm with id " + index);
                    }
                }
            }
        } else if (args[0].equals("placeDefault")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (args.length != 2) {
                sender.sendMessage("Not enough arguments");
                return false;
            }

            CRP_Dorms.dormList.get(Integer.parseInt(args[1])).placeToDefault();
            sender.sendMessage("Dorm at index: " + Integer.parseInt(args[1]) + " was placed to default");
        } else if (args[0].equals("setDefault")) {
            if (!sender.hasPermission("dorm.editor")) {
                sender.sendMessage(ChatColor.RED + "You don't have the right permissions");
            }
            
            if (args.length == 2) {
                CRP_Dorms.dormList.get(Integer.parseInt(args[1])).setCurrentToDefault();
                sender.sendMessage("Dorm at index: " + Integer.parseInt(args[1]) + " was reset to default");
            } else {
                sender.sendMessage("Not enough arguments");
            }
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> commandList = new ArrayList<>();
            if (sender.hasPermission("dorm.editor")) {
                commandList.add("saveData");
                commandList.add("allocate");
                commandList.add("createDormPlacement");
                commandList.add("save");
                commandList.add("list");
                commandList.add("show");
                commandList.add("stopShowing");
                commandList.add("playerList");
                commandList.add("setDefault");
                commandList.add("placeDefault");
            } else if (sender.hasPermission("dorm.editor")) {
                commandList.add("allocateSelf");
                commandList.add("edit");
            }
            return commandList;

        } else if (args[0].equals("allocate") && args.length == 2) {
            List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
            List<String> playerNames = new ArrayList<>();

            for (Player player : players) {
                playerNames.add(player.getName());
            }

            return playerNames;
        }

        return null;
    }
}