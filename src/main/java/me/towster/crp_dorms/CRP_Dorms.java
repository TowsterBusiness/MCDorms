package me.towster.crp_dorms;

import me.towster.crp_dorms.EventHandlers.onBlockPlace;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public final class CRP_Dorms extends JavaPlugin {
    public static List<Dorm> dormList = new ArrayList<>();
    public static HashMap<UUID, Integer> dormPlayerAllocation = new HashMap<>();
    public static HashMap<UUID, Dorm> highlightedDorms = new HashMap<>();
    public static List<Player> editingPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("dorm").setExecutor(new DormCommand());
        this.getServer().getPluginManager().registerEvents(new onBlockPlace(), this);

        System.out.println("CRP Dorms is online");

        System.out.println("Data Loading...");
        List<Dorm>  oldData = loadData();
        if (oldData != null) {
            dormList = oldData;
            System.out.println("Successfully loaded data");
        } else {
            System.out.println("No old data to load");
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run() {
                highlightedDorms.forEach((uuid, dorm) -> {
                    dorm.drawOutline();
                });
            }

        }, 0L, 10L);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run() {
                editingPlayers.forEach((player) -> {
                    dormPlayerAllocation.forEach((uuid, dormIndex) -> {
                        if (!(player.getUniqueId().equals(uuid) && dormList.get(dormIndex).isPlayerInDorm(player))) {
                            player.setGameMode(GameMode.SURVIVAL);
                            editingPlayers.remove(player);
                            player.sendMessage("You are out of your dorm. \nYou will be set back to normal");
                        }
                    });

                    player.getInventory().forEach((itemStack) -> {
                        if (itemStack.getType() == Material.COMMAND_BLOCK ||
                                itemStack.getType() == Material.COMMAND_BLOCK_MINECART ||
                                itemStack.getType() == Material.CHAIN_COMMAND_BLOCK ||
                                itemStack.getType() == Material.REPEATING_COMMAND_BLOCK) {
                            itemStack.setAmount(0);
                        }
                    });
                });
            }

        }, 0L, 5L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        saveData();

    }

    public static List<Dorm>  loadData() {
        File file = new File("plugins/CRP_Dorms", "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("dorm_positions")) {
            List<Dorm> dormListBuilder = new ArrayList<>();

            Set<String> indexes = config.getConfigurationSection("dorm_positions").getKeys(false);

            for (String index : indexes) {
                String path = "dorm_positions." + index;

                // Load world properties
                String worldName = config.getString(path + ".world_name");
                int doorX = config.getInt(path + ".door.x");
                int doorY = config.getInt(path + ".door.y");
                int doorZ = config.getInt(path + ".door.z");

                int left = config.getInt(path + ".left");
                int right = config.getInt(path + ".right");
                int depth = config.getInt(path + ".depth");
                int floor = config.getInt(path + ".floor");
                int height = config.getInt(path + ".height");
                String directionString = config.getString(path + ".direction");
                DIRECTION direction = DIRECTION.NORTH;
                switch (directionString) {
                    case "south":
                        direction = DIRECTION.SOUTH;
                        break;
                    case "east":
                        direction = DIRECTION.EAST;
                        break;
                    case "west":
                        direction = DIRECTION.WEST;
                        break;
                }

                World realWorld = null;

                List<World> worldList = Bukkit.getWorlds();
                for (World world : worldList) {
                    if (world.getName().equals(worldName)) {
                        realWorld = world;
                    }
                }

                Dorm newDorm = new Dorm(realWorld, left, right, height, depth, floor, new Vector(doorX, doorY, doorZ), direction);

                Set<String> defaultBlockIndices = config.getConfigurationSection(path + ".defaultBlocks").getKeys(false);

                List<MinimalBlock> defaultBlocks = new ArrayList<>();

                for (String defaultBlockPathIndex : defaultBlockIndices) {
                    String defaultBlockPath = path + ".defaultBlocks." + defaultBlockPathIndex;

                    // Load world properties
                    String data = config.getString(defaultBlockPath + ".data");
                    int blockX = config.getInt(defaultBlockPath + ".x");
                    int blockY = config.getInt(defaultBlockPath + ".y");
                    int blockZ = config.getInt(defaultBlockPath + ".z");

                    MinimalBlock block = new MinimalBlock(realWorld, blockX, blockY, blockZ, Bukkit.createBlockData(data));
                    defaultBlocks.add(block);
                }

                newDorm.defaultBlocks = defaultBlocks;

                dormListBuilder.add(newDorm);

                System.out.println("Loaded path: " + path);
            }
            return dormListBuilder;
        } else {
            System.out.println("No indexes found in the configuration!");
        }

        return null;
    }

    public static void saveData() {
        System.out.println("Saving data");

        // Create the file object
        File file = new File("plugins/CRP_Dorms", "config.yml");

        // Create the parent directories if they don't exist
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        // Load the YAML configuration
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (int index = 0; index < dormList.size(); index++) {
            String path = "dorm_positions." + index;
            Dorm dorm = dormList.get(index);

            config.set(path + ".world_name", dorm.world.getName());
            config.set(path + ".door.x", dorm.doorPos.getX());
            config.set(path + ".door.y", dorm.doorPos.getY());
            config.set(path + ".door.z", dorm.doorPos.getZ());
            config.set(path + ".left", dorm.left);
            config.set(path + ".right", dorm.right);
            config.set(path + ".depth", dorm.depth);
            config.set(path + ".floor", dorm.floor);
            config.set(path + ".height", dorm.height);

            String directionString = "north";

            if (dorm.direction == DIRECTION.EAST) {
                directionString = "east";
            } else if (dorm.direction == DIRECTION.SOUTH) {
                directionString = "south";
            } else if (dorm.direction == DIRECTION.WEST) {
                directionString = "west";
            }

            config.set(path + ".direction", directionString);

            int defaultBlockIndex = 0;
            for (MinimalBlock defaultBlock : dorm.defaultBlocks) {
                String defaultBlockPath = path + ".defaultBlocks." + defaultBlockIndex;
                config.set(defaultBlockPath + ".x", defaultBlock.x);
                config.set(defaultBlockPath + ".y", defaultBlock.y);
                config.set(defaultBlockPath + ".z", defaultBlock.z);
                config.set(defaultBlockPath + ".data", defaultBlock.data.getAsString(true));

                defaultBlockIndex ++;
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
