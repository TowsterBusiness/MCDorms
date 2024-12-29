package me.towster.crp_dorms;

import me.towster.crp_dorms.utils.ParticleDrawer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
enum DIRECTION {
    NORTH,
    SOUTH,
    EAST,
    WEST
}

public class Dorm {

    int left, right, height, depth, floor = 0;
    Vector doorPos;
    DIRECTION direction = DIRECTION.NORTH;
    World world;
    List<MinimalBlock> defaultBlocks = new ArrayList<>();

    public Dorm(World world, int left, int right, int height, int depth, int floor, Vector doorPos, DIRECTION direction) {
        this.left = left;
        this.right = right;
        this.height = height;
        this.floor = floor;
        this.depth = depth;
        this.doorPos = doorPos;
        this.world = world;
        this.direction = direction;
    }

    public void drawOutline() {
        List<Vector> corners = getCorners();
        corners.get(1).add(new Vector(1, 1, 1));

        ParticleDrawer.drawRectangularPrism(world, Particle.FLAME, corners.get(0), corners.get(1), 0.5);
        ParticleDrawer.drawRectangularPrism(world, Particle.DRAGON_BREATH, doorPos, doorPos.clone().add(new Vector(1, 2, 1)), 0.5);
    }

    public Vector getCenter() {
        List<Vector> corners = getCorners();

        return new Vector((corners.get(0).getX() + corners.get(1).getX()) / 2, (corners.get(0).getY() + corners.get(1).getY()) / 2, (corners.get(0).getZ() + corners.get(1).getZ()) / 2);
    }

    public List<MinimalBlock> getBlocks() {
        List<Vector> corners = getCorners();

        List<MinimalBlock> blocks = new ArrayList<>();

        for (int x = corners.get(0).getBlockX(); x <= corners.get(1).getBlockX(); x ++) {
            for (int y = corners.get(0).getBlockY(); y <= corners.get(1).getBlockY(); y ++) {
                for (int z = corners.get(0).getBlockZ(); z <= corners.get(1).getBlockZ(); z ++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getBlockData().getMaterial() != Material.AIR) {
                        blocks.add(new MinimalBlock(world, x, y, z, block.getBlockData()));
                    }
                }
            }

        }

        return blocks;
    }

    public void setCurrentToDefault() {
        defaultBlocks = getBlocks();
    }

    public void placeToDefault() {
        List<Vector> corners = getCorners();

        for (int x = corners.get(0).getBlockX(); x <= corners.get(1).getBlockX(); x ++) {
            for (int y = corners.get(0).getBlockY(); y <= corners.get(1).getBlockY(); y ++) {
                for (int z = corners.get(0).getBlockZ(); z <= corners.get(1).getBlockZ(); z ++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        defaultBlocks.forEach(MinimalBlock::place);
    }

    public Vector relativeVectorToWorld(Vector a) {
        Vector rotated;
        switch (direction) {
            case EAST:
                rotated = new Vector(-a.getBlockZ(), a.getBlockY(), -a.getBlockX());
                break;
            case WEST:
                rotated = new Vector(a.getBlockZ(), a.getBlockY(), -a.getBlockX());
                break;
            case SOUTH:
                rotated = new Vector(a.getBlockX(), a.getBlockY(), -a.getBlockZ());
                break;
            default:
                rotated = a;
        }

        return rotated.add(doorPos);
    }

    public List<Vector> getCorners() {
        List<Vector> output = new ArrayList<>();

        output.add(relativeVectorToWorld(new Vector(-left, -floor, 0)));
        output.add(relativeVectorToWorld(new Vector(right, height + 1, -depth)));

        List<Vector> output2 = new ArrayList<>();

        output2.add(new Vector(
                Math.min(output.get(0).getX(), output.get(1).getX()),
                Math.min(output.get(0).getY(), output.get(1).getY()),
                Math.min(output.get(0).getZ(), output.get(1).getZ())));
        output2.add(new Vector(
                Math.max(output.get(0).getX(), output.get(1).getX()),
                Math.max(output.get(0).getY(), output.get(1).getY()),
                Math.max(output.get(0).getZ(), output.get(1).getZ())));

        return output2;
    }

    public boolean isInDorm(Vector pos) {
        List<Vector> corners = getCorners();

        return pos.getX() >= corners.get(0).getX() &&
                pos.getX() <= corners.get(1).getX() &&
                pos.getY() >= corners.get(0).getY() &&
                pos.getY() <= corners.get(1).getY() &&
                pos.getZ() >= corners.get(0).getZ() &&
                pos.getZ() <= corners.get(1).getZ();
    }

    public boolean isPlayerInDorm(Player player) {
        return isInDorm(player.getLocation().toVector()) && world.equals(player.getWorld());
    }
}
