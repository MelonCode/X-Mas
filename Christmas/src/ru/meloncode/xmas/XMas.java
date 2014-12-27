package ru.meloncode.xmas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.meloncode.xmas.utils.TextUtils;

public class XMas {

	private static ConcurrentHashMap<UUID, MagicTree> trees = new ConcurrentHashMap<UUID, MagicTree>();

	public static ItemStack XMAS_CRYSTAL;

	public static void createMagicTree(Player player, Location loc) {
		MagicTree tree = new MagicTree(player.getUniqueId(), TreeLevel.SAPLING, loc);
		trees.put(tree.getTreeUID(), tree);
		tree.save();
	}

	public static void addMagicTree(MagicTree tree) {
		trees.put(tree.getTreeUID(), tree);
		tree.build();
	}

	public static Collection<MagicTree> getAllTrees() {
		return trees.values();
	}

	public static void removeTree(MagicTree tree) {
		tree.unbuild();
		TreeSerializer.removeTree(tree);
		trees.remove(tree.getTreeUID());
	}

	public static void processPresent(Block block, Player player) {
		if (block.getType() == Material.SKULL) {
			Skull skull = (Skull) block.getState();
			if (skull.getSkullType() == SkullType.PLAYER) {
				if (Main.getHeads().contains(skull.getOwner())) {
					if ((Main.RANDOM.nextFloat()) < Main.LUCK_CHANCE || !Main.LUCKCHANCEENABLED) {
						block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Main.gifts.get(Main.RANDOM.nextInt(Main.gifts.size()))));
						Effects.TREE_SWAG.playEffect(block.getLocation());
						TextUtils.sendMessage(player, LocaleManager.GIFT_LUCK);
					}
					else {
						Effects.SMOKE.playEffect(block.getLocation());
						block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COAL));
						TextUtils.sendMessage(player, LocaleManager.GIFT_FAIL);
					}
					block.setType(Material.AIR);
				}
			}
		}
	}

	public static List<MagicTree> getTreesPlayerOwn(Player player) {
		List<MagicTree> own = new ArrayList<MagicTree>();
		for (MagicTree cTree : getAllTrees())
			if (cTree.getOwner().equals(player.getUniqueId()))
				own.add(cTree);
		return own;
	}

	public static MagicTree getTree(UUID treeUID) {
		return trees.get(treeUID);
	}
}
