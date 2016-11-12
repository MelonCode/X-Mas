package ru.meloncode.xmas;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class StructureTemplate {
	HashMap<Vector, Material> struct;

	public StructureTemplate(HashMap<Vector, Material> hashMap) {
		this.struct = hashMap;
	}

	public void set(Vector vector, Material material) {
		struct.put(vector, material);
	}

	public Material get(Vector vec) {
		return struct.get(vec);
	}

	public Set<Vector> getAllLocations() {
		return struct.keySet();
	}

	public Collection<Material> getAllMaterials() {
		return struct.values();
	}

	public boolean canBeBuilded(Location start) {
		Location nLoc;
		for (Vector cVec : getAllLocations()) {
			nLoc = start.clone().add(cVec);
			if (nLoc.getBlock().getType().isSolid() || nLoc.getY() > nLoc.getWorld().getMaxHeight()) {
				if (getAllLocations().contains(nLoc)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param Location
	 *            where start to build
	 * @return Set of all placed blocks
	 */
	@SuppressWarnings("deprecation")
	public Set<Block> build(Location start) {
		Set<Block> blocks = new HashSet<Block>();
		Location cLoc;
		Block cBlock;
		for (Vector cVec : getAllLocations()) {
			cLoc = start.clone().add(cVec);
			cBlock = cLoc.getBlock();
			cBlock.setType(get(cVec));
			cBlock.setData((byte) 1);// TODO Fix quickfix
			blocks.add(cBlock);
		}

		return blocks;
	}

}
