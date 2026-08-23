package ru.meloncode.xmas;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.meloncode.xmas.utils.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Events implements Listener {
    private final Map<UUID, Long> destroyers = new HashMap<>();

    public void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJournalInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof JournalInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJournalInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof JournalInventoryHolder) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerOpenPresent(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return; //Event firing for both hands
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (XMas.isPresentHead(block)) {
                XMas.processPresent(block, event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerOpenPresent(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (XMas.isPresentHead(block)) {
            event.setDropItems(false);
            XMas.processPresent(block, event.getPlayer());
        }
    }

    // Prevent bonemeal on magic tree
    @EventHandler
    public void onPlayerUseBonemeal(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            if (MagicTree.isBlockBelongs(event.getClickedBlock()))
                if (event.getItem() != null)
                    if (event.getItem().getType() == Material.BONE_MEAL)
                        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerClickBlock(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return; //Event firing for both hands
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }
            if (MagicTree.isBlockBelongs(block)) {
                event.setCancelled(true);
                MagicTree tree = MagicTree.getTreeByBlock(block);
                if (tree == null) {
                    return;
                }
                if (Main.inProgress) {
                    if (tree.getLevel().hasNext()) {
                        if (tree.canLevelUp()) {
                            if (!tree.tryLevelUp()) {
                                TextUtils.sendMessage(player, LocaleManager.GROW_NOT_ENOUGH_PLACE);
                            }
                        } else {
                            if (event.getItem() != null) {
                                ItemStack is = event.getItem();
                                if (tree.grow(is.getType())) {
                                    TextUtils.sendMessage(player, LocaleManager.GROW_LVL_PROGRESS);
                                    if (player.getGameMode() != GameMode.CREATIVE) {
                                        if (is.getAmount() > 1) {
                                            is.setAmount(is.getAmount() - 1);
                                        } else {
                                            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                        }
                                    }
                                }
                            }
                            if (tree.level.nextLevel != null) {
                                TextUtils.sendMessage(player, LocaleManager.GROW_LVL_PROGRESS);
                                for (var line : TextUtils.generateChatReqList(tree)) {
                                    TextUtils.sendMessage(player, line);
                                }

                                if (tree.getLevelupRequirements().size() == 0) {
                                    TextUtils.sendMessage(player, LocaleManager.GROW_LVL_READY);
                                }
                            }
                        }
                    } else {
                        TextUtils.sendMessage(player, LocaleManager.GROW_LEVEL_MAX);
                    }
                } else {
                    if (player.getUniqueId().equals(tree.getOwner())) {
                        tree.end(player);
                        TextUtils.sendMessage(player, LocaleManager.TIMEOUT);
                    } else {
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_FAIL_OWNER);
                    }
                }
            } else {
                if (block.getType() == Material.SPRUCE_SAPLING) {
                    ItemStack is = event.getItem();
                    if (is != null)
                        if (Main.inProgress) {
                            if (XMas.getTreesPlayerOwn(player).size() < Main.MAX_TREE_COUNT) {
                                if (is.getType() == XMas.XMAS_CRYSTAL.getType() && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                                    ItemMeta im = is.getItemMeta();
                                    if (im != null && im.getPersistentDataContainer().has(Main.getCrystalKey(), PersistentDataType.BYTE)) {
                                        if (player.getGameMode() != GameMode.CREATIVE) {
                                            if (is.getAmount() > 1) {
                                                is.setAmount(is.getAmount() - 1);
                                            } else {
                                                event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                            }
                                        }
                                        XMas.createMagicTree(player, block.getLocation());
                                    }
                                }
                            } else {
                                TextUtils.sendMessage(player, LocaleManager.TREE_LIMIT);
                            }
                        } else {
                            TextUtils.sendMessage(player, LocaleManager.TIMEOUT);
                        }
                }
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (MagicTree.isBlockBelongs(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockBreakByExplosion(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (MagicTree.isBlockBelongs(block))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (MagicTree.isBlockBelongs(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (MagicTree.isBlockBelongs(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (MagicTree.isBlockBelongs(event.getBlock())) {
            event.setCancelled(true);
        }
        for (BlockFace face : BlockFace.values()) {
            if (MagicTree.isBlockBelongs(event.getBlock().getRelative(face))) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (MagicTree.isBlockBelongs(block)) {
            event.setCancelled(true);
            MagicTree tree = MagicTree.getTreeByBlock(block);
            switch (block.getType()) {
                case SPRUCE_LOG:
                    if (player.getUniqueId().equals(tree.getOwner()) || XMasCommand.canOverrideTree(player)) {
                        if (Main.inProgress)
                            if (destroyers.containsKey(player.getUniqueId()) && System.currentTimeMillis() - destroyers.get(player.getUniqueId()) <= 10000) {
                                if (Main.resourceBack) {
                                    tree.end(player);
                                } else {
                                    XMas.removeTree(tree);
                                }
                                if (Main.inProgress)
                                    TextUtils.sendMessage(player, LocaleManager.DESTROY_COMPLETE);
                            } else {
                                destroyers.put(player.getUniqueId(), System.currentTimeMillis());
                                if (Main.inProgress)
                                    TextUtils.sendMessage(player, LocaleManager.DESTROY_WARNING);
                                TextUtils.sendMessage(player, LocaleManager.DESTROY_TUT);
                                if (Main.resourceBack) {
                                    TextUtils.sendMessage(player, LocaleManager.DESTROY_RESOURCE_BACK);
                                }
                            }
                        else {
                            tree.end(player);
                        }
                    } else {
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_FAIL_OWNER);
                    }
                    break;
                case SPRUCE_LEAVES:
                case GLOWSTONE:
                    if (Main.inProgress)
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_LEAVES_SANTA);
                    if (player.getUniqueId().equals(tree.getOwner()) || XMasCommand.canOverrideTree(player)) {
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_LEAVES_TUT);
                    } else {
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_FAIL_OWNER);
                    }
                    break;
                case SPRUCE_SAPLING:
                    if (player.getUniqueId().equals(tree.getOwner()) || XMasCommand.canOverrideTree(player)) {
                        if (Main.inProgress) {
                            if (destroyers.containsKey(player.getUniqueId()) && System.currentTimeMillis() - destroyers.get(player.getUniqueId()) <= 10000) {
                                if (Main.resourceBack) {
                                    tree.end(player);
                                } else {
                                    XMas.removeTree(tree);
                                }
                                TextUtils.sendMessage(player, LocaleManager.DESTROY_COMPLETE);
                            } else {
                                destroyers.put(player.getUniqueId(), System.currentTimeMillis());
                                if (Main.inProgress)
                                    TextUtils.sendMessage(player, LocaleManager.DESTROY_SAPLING);
                                TextUtils.sendMessage(player, LocaleManager.DESTROY_TUT);
                                if (Main.resourceBack) {
                                    TextUtils.sendMessage(player, LocaleManager.DESTROY_RESOURCE_BACK);
                                }
                            }
                        } else {
                            tree.end(player);
                        }
                    } else {
                        TextUtils.sendMessage(player, LocaleManager.DESTROY_FAIL_OWNER);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onSaplingGrow(StructureGrowEvent event) {
        // Prevent magic tree to grow as normal
        if (event.getSpecies() == TreeType.REDWOOD) {
            if (MagicTree.isBlockBelongs(event.getLocation().getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void disableDecay(LeavesDecayEvent e)
    {
        if(e.isCancelled())
            return;

        if(e.getBlock().getType() != Material.SPRUCE_LEAVES)
            return;

        if (MagicTree.isBlockBelongs(e.getBlock().getLocation().getBlock()))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void disableFireworkDamage(EntityDamageByEntityEvent e)
    {
        if (e.getDamager().getType() == EntityType.FIREWORK_ROCKET
                && e.getDamager() instanceof Firework firework
                && firework.getPersistentDataContainer().has(Main.getNoDamageFireworkKey(), PersistentDataType.BYTE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void chunkLoad(ChunkLoadEvent e)
    {
        Collection<MagicTree> trees = XMas.getAllTreesInChunk(e.getChunk());
        if(trees == null)
            return;
        for(MagicTree tree : trees)
        {
            if(tree.hasScheduledPresents())
                tree.spawnScheduledPresents();
        }
    }
}
