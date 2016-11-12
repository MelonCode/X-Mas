package ru.meloncode.xmas;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public class TreeLevel {
	public static TreeLevel MAGIC_TREE;
	public static TreeLevel TREE;
	public static TreeLevel SMALL_TREE;
	public static TreeLevel SAPLING;

	String levelName;
	TreeLevel nextLevel;
	long giftDelay;
	Map<Material, Integer> levelupRequirements;
	StructureTemplate structure;
	ParticleContainer ambientEffect;
	ParticleContainer swagEffect;
	ParticleContainer bodyEffect;
	private int height;

	public TreeLevel(String levelName, ParticleContainer ambientEffect, ParticleContainer swagEffect, ParticleContainer bodyEffect, TreeLevel nextLevel, long giftDelay, Map<Material, Integer> levelupRequirements, StructureTemplate structure) {
		this.levelName = levelName;
		this.nextLevel = nextLevel;
		this.giftDelay = giftDelay;
		this.levelupRequirements = levelupRequirements;
		this.structure = structure;
		this.ambientEffect = ambientEffect;
		this.swagEffect = swagEffect;
		this.bodyEffect = bodyEffect;
		height = -256;
		for (Vector vector : structure.getAllLocations()) {
			if (vector.getY() > height)
				height = (int) vector.getY();
		}
	}

	public String getLevelName() {
		return levelName;
	}

	public TreeLevel getNextLevel() {
		return nextLevel;
	}

	public ParticleContainer getAmbientEffect() {
		return ambientEffect;
	}

	public ParticleContainer getSwagEffect() {
		return swagEffect;
	}

	public ParticleContainer getBodyEffect() {
		return bodyEffect;
	}

	public Map<Material, Integer> getLevelupRequirements() {
		return levelupRequirements;
	}

	public StructureTemplate getStructureTemplate() {
		return structure;
	}

	public int getTreeHeight() {
		return height;
	}

	public static TreeLevel fromString(String name) {
		if (name == null)
			throw new IllegalArgumentException("Level name cannot be null!");
		switch (name.toLowerCase()) {
		case "sapling":
			return SAPLING;
		case "tree":
			return TREE;
		case "small_tree":
			return SMALL_TREE;
		case "magic_tree":
			return MAGIC_TREE;
		default:
			throw new IllegalArgumentException("Wrong level '" + name + "'");
		}
	}

	public long getGiftDelay() {
		return giftDelay;
	}

	public boolean hasNext() {
		return nextLevel != null;
	}

}
