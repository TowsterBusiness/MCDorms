package me.towster.crp_dorms.utils;

import jdk.incubator.vector.VectorOperators;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleDrawer {
    public static void drawLine(World world, Particle particle, Vector a, Vector b, double margin) {

        Location loc = new Location(world, a.getX(), a.getY(), a.getZ());
        Vector mult = b.clone().subtract(a).normalize().multiply(margin);

        for (int i = 0; i < (int) Math.floor(a.distance(b) / margin); i++) {
            world.spawnParticle(particle, loc.add(mult), 0, 0, 0, 0);
        }

        world.spawnParticle(particle, new Location(world, b.getX(), b.getY(), b.getZ()), 0, 0, 0, 0);
    }

    public static void drawCyl(World world, Particle particle, Vector a, Vector b, double radius, double margin) {

        Location loc = new Location(world, a.getX(), a.getY(), a.getZ());
        Vector mult = b.clone().subtract(a).normalize().multiply(margin);

        for (int i = 0; i < (int) Math.floor(a.distance(b) / margin); i++) {
            world.spawnParticle(particle, loc.add(mult), 0, 0, 0, 0);
        }

        world.spawnParticle(particle, new Location(world, b.getX(), b.getY(), b.getZ()), 0, 0, 0, 0);
    }

    public static void drawRectangularPrism(World world, Particle particle, Vector a, Vector b, double margin) {
        Vector loc1 = new Vector(a.getX(), a.getY(), a.getZ());
        Vector loc2 = new Vector(a.getX(), a.getY(), b.getZ());
        Vector loc3 = new Vector(a.getX(), b.getY(), a.getZ());
        Vector loc4 = new Vector(b.getX(), a.getY(), a.getZ());
        Vector loc5 = new Vector(b.getX(), b.getY(), b.getZ());
        Vector loc6 = new Vector(b.getX(), b.getY(), a.getZ());
        Vector loc7 = new Vector(b.getX(), a.getY(), b.getZ());
        Vector loc8 = new Vector(a.getX(), b.getY(), b.getZ());

        drawLine(world, particle, loc1, loc2, margin);
        drawLine(world, particle, loc1, loc3, margin);
        drawLine(world, particle, loc1, loc4, margin);
        drawLine(world, particle, loc5, loc6, margin);
        drawLine(world, particle, loc5, loc7, margin);
        drawLine(world, particle, loc5, loc8, margin);

        drawLine(world, particle, loc2, loc7, margin);
        drawLine(world, particle, loc2, loc8, margin);
        drawLine(world, particle, loc3, loc6, margin);
        drawLine(world, particle, loc3, loc8, margin);
        drawLine(world, particle, loc4, loc6, margin);
        drawLine(world, particle, loc4, loc7, margin);
    }
}
