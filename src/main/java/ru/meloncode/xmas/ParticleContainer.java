package ru.meloncode.xmas;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import java.util.Random;

public class ParticleContainer {

    final static DustOptions[] COLORS = new DustOptions[]{
            new DustOptions(Color.LIME, 1f),
            new DustOptions(Color.RED, 1f),
            new DustOptions(Color.AQUA, 1f),
            new DustOptions(Color.YELLOW, 1f),
            new DustOptions(Color.BLUE, 1f),
            new DustOptions(Color.FUCHSIA, 1f)
    };
    final static Random random = new Random("Happy 2020!".hashCode());

    private final Particle type;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int count;

    public ParticleContainer(Particle type, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    public void playEffect(Location location) {
        location = location.clone(); // prevent chaning pos of object
        location.add(0.5, 0.5, 0.5); // A small fix
        for (Player player : location.getWorld().getPlayers())
            if (player.getLocation().distance(location) < 16) {
                try {
                    if (type == Particle.REDSTONE) {
                        player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, COLORS[random.nextInt(6)]);
                    } else {
                        player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
}
