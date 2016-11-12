package ru.meloncode.xmas;

import org.bukkit.Particle;

class Effects {

    public static final ParticleContainer AMBIENT_SAPLING = new ParticleContainer(Particle.PORTAL, 0.2f, 0.25f, 0.2f, 0.1f, 16);
    public static final ParticleContainer AMBIENT_PORTAL = new ParticleContainer(Particle.PORTAL, 2f, 2f, 2f, 0.1f, 16);
    public static final ParticleContainer TREE_SWAG = new ParticleContainer(Particle.REDSTONE, 0.25f, 0.25f, 0.25f, 10f, 16);
    public static final ParticleContainer TREE_HEARTS_AMBIENT = new ParticleContainer(Particle.HEART, 1.25f, 1.25f, 1.25f, 10f, 1);
    public static final ParticleContainer TREE_RED_SWAG = new ParticleContainer(Particle.REDSTONE, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer TREE_WHITE_AMBIENT = new ParticleContainer(Particle.FIREWORKS_SPARK, 2.25f, 2.25f, 2.25f, 0f, 4);
    public static final ParticleContainer TREE_CRIT_SWAG = new ParticleContainer(Particle.CRIT, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer TREE_GOLD_SWAG = new ParticleContainer(Particle.FLAME, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer SMOKE = new ParticleContainer(Particle.SMOKE_LARGE, 0f, 0f, 0f, 0f, 16);

    public static final ParticleContainer GROW = new ParticleContainer(Particle.VILLAGER_HAPPY, 0.25f, 0.25f, 0.25f, 1f, 16);
    public static final ParticleContainer AMBIENT_SNOW = new ParticleContainer(Particle.SNOW_SHOVEL, 1.5f, 3f, 1.5f, 0, 16);

}
