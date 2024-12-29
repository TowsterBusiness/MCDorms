package me.towster.crp_dorms;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public class MinimalBlock {
    int x;
    int y;
    int z;
    BlockData data;
    BlockFace face = null;
    World world;

    public MinimalBlock(World world, int x, int y, int z, BlockData data) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;

        if (data instanceof Container) {
            ((Container) data).getInventory().clear();
        }
    }

    public void place() {
        Block thisBlock = world.getBlockAt(x, y, z);
        thisBlock.setBlockData(data);
    }
}
