package ru.meloncode.xmas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StructureTemplate {
    private final HashMap<Vector, Material> struct;

    public StructureTemplate(HashMap<Vector, Material> hashMap) {
        this.struct = hashMap;
    }

    public void set(Vector vector, Material material) {
        struct.put(vector, material);
    }

    private Material get(Vector vec) {
        return struct.get(vec);
    }

    public Set<Vector> getAllLocations() {
        return struct.keySet();
    }

    public Collection<Material> getAllMaterials() {
        return struct.values();
    }

    public boolean canGrow(Location start) {
        Vector nLoc;
        for (Vector cVec : getAllLocations()) {
            nLoc = start.toVector().add(cVec);
            if (start.getBlock().getType().isSolid() || nLoc.getY() > start.getWorld().getMaxHeight()) {
                return !getAllLocations().contains(nLoc);
            }
        }
        return true;
    }

    /**
     * @param start where start to build
     * @return Set of all placed blocks
     */
    public Set<Block> build(Location start) {
        Set<Block> blocks = new HashSet<>();
        Location cLoc;
        Block cBlock;
        for (Vector cVec : getAllLocations()) {
            cLoc = start.clone().add(cVec);
            cBlock = cLoc.getBlock();
            cBlock.setType(get(cVec));
            cBlock.setData((byte) 1);
            blocks.add(cBlock);
        }

        return blocks;
    }

}
