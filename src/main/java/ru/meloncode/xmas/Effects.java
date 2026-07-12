package ru.meloncode.xmas;

import org.bukkit.Particle;

class Effects {

    public static final ParticleContainer AMBIENT_SAPLING = new ParticleContainer(Particle.PORTAL, 0.2f, 0.25f, 0.2f, 0.1f, 16);
    public static final ParticleContainer AMBIENT_PORTAL = new ParticleContainer(Particle.PORTAL, 2f, 2f, 2f, 0.1f, 16);
    public static final ParticleContainer TREE_SWAG = new ParticleContainer(Particle.DUST, 0.25f, 0.25f, 0.25f, 10f, 16);
    public static final ParticleContainer TREE_HEARTS_AMBIENT = new ParticleContainer(Particle.HEART, 1.25f, 1.25f, 1.25f, 10f, 1);
    public static final ParticleContainer TREE_RED_SWAG = new ParticleContainer(Particle.DUST, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer TREE_WHITE_AMBIENT = new ParticleContainer(Particle.FIREWORK, 2.25f, 2.25f, 2.25f, 0f, 4);
    public static final ParticleContainer TREE_CRIT_SWAG = new ParticleContainer(Particle.CRIT, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer TREE_GOLD_SWAG = new ParticleContainer(Particle.FLAME, 0.25f, 0.25f, 0.25f, 0f, 16);
    public static final ParticleContainer SMOKE = new ParticleContainer(Particle.LARGE_SMOKE, 0f, 0f, 0f, 0f, 16);

    public static final ParticleContainer GROW = new ParticleContainer(Particle.HAPPY_VILLAGER, 0.25f, 0.25f, 0.25f, 1f, 16);
    public static final ParticleContainer AMBIENT_SNOW = new ParticleContainer(Particle.ITEM_SNOWBALL, 1.5f, 3f, 1.5f, 0, 16);
    public static final ParticleContainer CRYSTAL_SAPLING_GREEN = new ParticleContainer(Particle.SPORE_BLOSSOM_AIR, 0.45f, 0.55f, 0.45f, 0.02f, 12);
    public static final ParticleContainer CRYSTAL_SAPLING_WHITE = new ParticleContainer(Particle.FIREWORK, 0.35f, 0.45f, 0.35f, 0.01f, 8);

}
