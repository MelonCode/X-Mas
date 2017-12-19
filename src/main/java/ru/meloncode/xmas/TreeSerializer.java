package ru.meloncode.xmas;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class TreeSerializer {
    private static final File treesFile = new File(Main.getInstance().getDataFolder() + "/trees.yml");
    private static final FileConfiguration trees = ConfigUtils.loadConfig(treesFile);

    public static void loadTrees(JavaPlugin plugin, World world) {
        try {
            UUID owner;
            UUID treeUID;
            TreeLevel level;
            int x, y, z;
            Location loc;
            if (trees.getConfigurationSection("trees") != null && trees.getConfigurationSection("trees").getKeys(false).size() > 0) {

                for (String cKey : trees.getConfigurationSection("trees").getKeys(false)) {
                    if (world.getName().equals(trees.getString("trees." + cKey + ".loc.world"))) {
                        try {
                            treeUID = UUID.fromString(cKey);
                            owner = UUID.fromString(trees.getString("trees." + cKey + ".owner"));
                            level = TreeLevel.fromString(trees.getString("trees." + cKey + ".level"));
                            x = trees.getInt("trees." + cKey + ".loc.x");
                            y = trees.getInt("trees." + cKey + ".loc.y");
                            z = trees.getInt("trees." + cKey + ".loc.z");
                            loc = new Location(world, x, y, z);
                            Map<Material, Integer> requirements;
                            if (trees.getConfigurationSection("trees." + cKey + ".levelup") != null) {
                                requirements = convertRequirementsMap(trees.getConfigurationSection("trees." + cKey + ".levelup").getValues(false));
                            } else {
                                requirements = new HashMap<>();
                            }
                            XMas.addMagicTree(new MagicTree(owner, treeUID, level, loc, requirements));
                        } catch (Exception e) {
                            plugin.getLogger().severe(String.format("Error while loading tree `%s`", cKey));
                            e.printStackTrace();
                            System.out.println("================================================");
                        }
                    }
                }
            }
        } catch (Exception e) {
            TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "ERROR WHILE LOADING TREES");
            e.printStackTrace();
        }

    }

    public static void saveTree(MagicTree tree) {
        String cKey = tree.getTreeUID().toString();
        String owner = tree.getOwner().toString();
        trees.set("trees." + cKey + ".owner", owner);
        trees.set("trees." + cKey + ".level", tree.getLevel().getLevelName());
        trees.set("trees." + cKey + ".loc.world", tree.getLocation().getWorld().getName());
        trees.set("trees." + cKey + ".loc.x", tree.getLocation().getX());
        trees.set("trees." + cKey + ".loc.y", tree.getLocation().getY());
        trees.set("trees." + cKey + ".loc.z", tree.getLocation().getZ());
        if (tree.getLevelupRequirements() != null && tree.getLevelupRequirements().size() > 0)
            trees.createSection("trees." + cKey + ".levelup", tree.getLevelupRequirements());
        try {
            trees.save(treesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeTree(MagicTree tree) {
        trees.set("trees." + tree.getTreeUID().toString(), null);
        try {
            trees.save(treesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Material, Integer> convertRequirementsMap(Map<String, Object> map) {
        Map<Material, Integer> levelupRequirements = new HashMap<>();
        Material cMaterial;
        int value;
        if (map != null)
            for (String sMaterial : map.keySet()) {
                try {
                    cMaterial = Material.valueOf(sMaterial);
                    value = (int) map.get(sMaterial);
                    levelupRequirements.put(cMaterial, value);
                } catch (IllegalArgumentException e) {
                    TextUtils.sendConsoleMessage("Can't find material '" + sMaterial + "' for tree level.");
                    return null;
                }
            }
        return levelupRequirements;
    }
}