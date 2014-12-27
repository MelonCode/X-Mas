package ru.meloncode.xmas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.TextUtils;

public class TreeSerializer {
	private static File treesFile = new File(Main.getInstance().getDataFolder() + "/trees.yml");
	public static FileConfiguration trees = ConfigUtils.loadConfig(treesFile);

	public static void loadTrees() {
		try {
			UUID owner;
			UUID treeUID;
			TreeLevel level;
			int x, y, z;
			World world;
			Location loc;
			if (trees.getConfigurationSection("trees") != null && trees.getConfigurationSection("trees").getKeys(false).size() > 0) {
				for (String cKey : trees.getConfigurationSection("trees").getKeys(false)) {
					treeUID = UUID.fromString(cKey);
					owner = UUID.fromString(trees.getString("trees." + cKey + ".owner"));
					level = TreeLevel.fromString(trees.getString("trees." + cKey + ".level"));
					world = Bukkit.getWorld(trees.getString("trees." + cKey + ".loc.world"));
					x = trees.getInt("trees." + cKey + ".loc.x");
					y = trees.getInt("trees." + cKey + ".loc.y");
					z = trees.getInt("trees." + cKey + ".loc.z");
					loc = new Location(world, x, y, z);
					Map<Material, Integer> requirements;
					if (trees.getConfigurationSection("trees." + cKey + ".levelup") != null) {
						requirements = convertRequirementsMap(trees.getConfigurationSection("trees." + cKey + ".levelup").getValues(false));
					}
					else {
						requirements = new HashMap<Material, Integer>();
					}
					XMas.addMagicTree(new MagicTree(owner, treeUID, level, loc, requirements));
				}
			}
			else {
				TextUtils.sendConsoleMessage("No trees to load");
			}
		}
		catch (Exception e) {
			TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "ERROR WHILE LOADING TREES");
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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removeTree(MagicTree tree) {
		trees.set("trees." + tree.getTreeUID().toString(), null);
		try {
			trees.save(treesFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<Material, Integer> convertRequirementsMap(Map<String, Object> map) {
		Map<Material, Integer> levelupRequirements = new HashMap<Material, Integer>();
		Material cMaterial;
		int value;
		if (map != null)
			for (String sMaterial : map.keySet()) {
				try {
					cMaterial = Material.valueOf(sMaterial);
					value = (int) map.get(sMaterial);
					levelupRequirements.put(cMaterial, value);
				}
				catch (IllegalArgumentException e) {
					TextUtils.sendConsoleMessage("Can't find material '" + sMaterial + "' for tree level.");
					return null;
				}
			}
		return levelupRequirements;
	}
}