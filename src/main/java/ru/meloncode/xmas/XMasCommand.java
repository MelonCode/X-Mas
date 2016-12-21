package ru.meloncode.xmas;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class XMasCommand implements CommandExecutor {

    private final Main plugin;

    private XMasCommand(Main plugin) {
        this.plugin = plugin;
    }

    public static void register(Main plugin) {
        plugin.getCommand("xmas").setExecutor(new XMasCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            String action = args[0].toLowerCase();
            switch (action) {
                case "help": {
                    for (String line : LocaleManager.COMMAND_HELP) {
                        sender.sendMessage(GREEN + line);
                    }
                    break;
                }
                case "give": {
                    if (args.length > 1) {
                        String name = args[1];
                        Player player = Bukkit.getPlayer(name);
                        if (player != null) {
                            player.getInventory().addItem(XMas.XMAS_CRYSTAL);
                        } else {
                            sender.sendMessage(LocaleManager.COMMAND_PLAYER_OFFLINE);
                        }
                    } else {
                        sender.sendMessage(LocaleManager.COMMAND_NO_PLAYER_NAME);
                    }
                    break;
                }
                case "end": {
                    plugin.end();
                    break;
                }
                case "gifts": {
                    Random random = new Random();
                    for (MagicTree magicTree : XMas.getAllTrees()) {
                        for (int i = 0; i < 3 + random.nextInt(4); i++) {
                            magicTree.spawnPresent();
                        }
                    }
                    Bukkit.broadcastMessage(LocaleManager.COMMAND_GIVEAWAY);
                    break;
                }

                default:
                    return false;
            }
        } else {
            sendStatus(sender);
        }
        return true;
    }

    private void sendStatus(CommandSender sender) {

        int treeCount = XMas.getAllTrees().size();
        Set<UUID> owners = new HashSet<>();
        for (MagicTree magicTree : XMas.getAllTrees()) {
            owners.add(magicTree.getOwner());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");

        sender.sendMessage(DARK_GREEN + LocaleManager.PLUGIN_NAME + " " + plugin.getDescription().getVersion() + " Plugin Status");
        sender.sendMessage("");
        sender.sendMessage(GRAY + "Event Status: " + (Main.inProgress ? DARK_GREEN + "In Progress" : RED + "Holidays End"));
        if (Main.inProgress) {
            sender.sendMessage(DARK_GREEN + "Current Time: " + GREEN + sdf.format(System.currentTimeMillis()));
            sender.sendMessage(DARK_GREEN + "Holidays end: " + RED + sdf.format(Main.endTime));
        }
        sender.sendMessage(GREEN + "Auto-End: " + (Main.autoEnd ? DARK_GREEN + "Yes" : RED + "No") + GREEN + "    |    " + "Resource Back: " + (Main.resourceBack ? DARK_GREEN + "Yes" : "No"));
        sender.sendMessage("");
        sender.sendMessage(DARK_GREEN + "There are " + GREEN + treeCount + DARK_GREEN + " magic trees owned by " + RED + owners.size() + DARK_GREEN + " players");
        sender.sendMessage(DARK_GREEN + "Use " + RED + "/xmas help" + DARK_GREEN + " for command list");

    }

}
