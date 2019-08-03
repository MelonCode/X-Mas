package ru.meloncode.xmas;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.TileEntitySkull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.meloncode.xmas.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class XMas {

    private static final ConcurrentHashMap<UUID, MagicTree> trees = new ConcurrentHashMap<>();
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
        if (block.getType() == Material.PLAYER_HEAD) {
            Skull skull = (Skull) block.getState();
            TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld)skull.getWorld()).getHandle().getTileEntity(new BlockPosition(skull.getX(), skull.getY(), skull.getZ()));
            if(skullTile != null && skullTile.gameProfile != null) {
                if (Main.getHeads().contains(skullTile.gameProfile.getName())) {
                    Location loc = block.getLocation();
                    if ((Main.RANDOM.nextFloat()) < Main.LUCK_CHANCE || !Main.LUCK_CHANCE_ENABLED) {
                        loc.getWorld().dropItemNaturally(loc,
                                new ItemStack(Main.gifts.get(Main.RANDOM.nextInt(Main.gifts.size()))));
                        Effects.TREE_SWAG.playEffect(loc);
                        TextUtils.sendMessage(player, LocaleManager.GIFT_LUCK);
                    } else {
                        Effects.SMOKE.playEffect(loc);
                        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.COAL));
                        TextUtils.sendMessage(player, LocaleManager.GIFT_FAIL);
                    }
                    block.setType(Material.AIR);
                }
            }
        }
    }

    public static List<MagicTree> getTreesPlayerOwn(Player player) {
        List<MagicTree> own = new ArrayList<>();
        for (MagicTree cTree : getAllTrees())
            if (cTree.getOwner().equals(player.getUniqueId()))
                own.add(cTree);
        return own;
    }

    public static MagicTree getTree(UUID treeUID) {
        return trees.get(treeUID);
    }
}
