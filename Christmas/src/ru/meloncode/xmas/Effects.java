package ru.meloncode.xmas;

import ru.meloncode.xmas.ParticleEffect.EffectType;

public class Effects {

	public static final ParticleEffect AMBIENT_SAPLING = new ParticleEffect(EffectType.PORTAL, 0.2f, 0.25f, 0.2f, 0.1f, 16);
	public static final ParticleEffect AMBIENT_PORTAL = new ParticleEffect(EffectType.PORTAL, 2f, 2f, 2f, 0.1f, 16);
	public static final ParticleEffect TREE_SWAG = new ParticleEffect(EffectType.RED_DUST, 0.25f, 0.25f, 0.25f, 10f, 16);
	public static final ParticleEffect TREE_HEARTS_AMBIENT = new ParticleEffect(EffectType.HEART, 1.25f, 1.25f, 1.25f, 10f, 1);
	public static final ParticleEffect TREE_RED_SWAG = new ParticleEffect(EffectType.RED_DUST, 0.25f, 0.25f, 0.25f, 0f, 16);
	public static final ParticleEffect TREE_WHITE_AMBIENT = new ParticleEffect(EffectType.FIREWORKS_SPARK, 2.25f, 2.25f, 2.25f, 0f, 4);
	public static final ParticleEffect TREE_CRIT_SWAG = new ParticleEffect(EffectType.CRIT, 0.25f, 0.25f, 0.25f, 0f, 16);
	public static final ParticleEffect TREE_GOLD_SWAG = new ParticleEffect(EffectType.FLAME, 0.25f, 0.25f, 0.25f, 0f, 16);
	public static final ParticleEffect SMOKE = new ParticleEffect(EffectType.LARGE_SMOKE, 0f, 0f, 0f, 0f, 16);

	public static final ParticleEffect GROW = new ParticleEffect(EffectType.HAPPY_VILLAGER, 0.25f, 0.25f, 0.25f, 1f, 16);
	public static final ParticleEffect AMBIENT_SNOW = new ParticleEffect(EffectType.SNOW_SHOVEL, 1.5f, 3f, 1.5f, 0, 16);

}
