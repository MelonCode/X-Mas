package ru.meloncode.xmas;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleContainer {

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
                    player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
}
