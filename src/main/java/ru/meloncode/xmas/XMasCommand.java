package ru.meloncode.xmas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMasCommand implements CommandExecutor, TabCompleter {
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    public static final String PRIMARY_COMMAND = "xmastree";
    private static final String RETIRED_LEGACY_COMMAND = "xmas";
    public static final String PERMISSION_ADMIN = "onembxmastree.admin";
    public static final String PERMISSION_STATUS = "onembxmastree.command.status";
    public static final String PERMISSION_HELP = "onembxmastree.command.help";
    public static final String PERMISSION_INFO = "onembxmastree.command.info";
    public static final String PERMISSION_GIVE = "onembxmastree.command.give";
    public static final String PERMISSION_GIFTS = "onembxmastree.command.gifts";
    public static final String PERMISSION_ADDHAND = "onembxmastree.command.addhand";
    public static final String PERMISSION_RELOAD = "onembxmastree.command.reload";
    public static final String PERMISSION_DEBUG = "onembxmastree.command.debug";
    public static final String PERMISSION_DEBUG_TOGGLE = "onembxmastree.command.debug.toggle";
    public static final String PERMISSION_INSPECT = "onembxmastree.command.inspect";
    public static final String PERMISSION_TEST = "onembxmastree.command.test";
    public static final String PERMISSION_DATA = "onembxmastree.command.data";
    public static final String PERMISSION_JOURNAL = "onembxmastree.command.journal";
    public static final String PERMISSION_MILESTONES = "onembxmastree.command.milestones";
    public static final String PERMISSION_MILESTONES_CLAIM = "onembxmastree.command.milestones.claim";
    public static final String PERMISSION_COMMUNITY = "onembxmastree.command.community";
    public static final String PERMISSION_COMMUNITY_CLAIM = "onembxmastree.command.community.claim";
    public static final String PERMISSION_END = "onembxmastree.command.end";
    public static final String PERMISSION_TREE_OVERRIDE = "onembxmastree.tree.override";
    private static final List<String> COMMANDS = Arrays.asList("help", "info", "give", "end", "gifts", "reload", "addhand", "debug", "inspect", "test", "data", "journal", "milestones", "community");
    private static final Set<String> DEBUG_TOGGLE_KEYS = new LinkedHashSet<>(Arrays.asList(
            "core.plugin-enabled",
            "core.holiday-ends.enabled",
            "core.holiday-ends.resource-back",
            "core.particles-enabled",
            "xmas.luck.enabled"
    ));
    private static final Map<String, String> DEBUG_SECTIONS = createDebugSections();
    private static XMasCommand registeredExecutor;

    private final Main plugin;

    private XMasCommand(Main plugin) {
        this.plugin = plugin;
    }

    public static void register(Main plugin) {
        registeredExecutor = new XMasCommand(plugin);
        PluginCommand primaryCommand = plugin.getCommand(PRIMARY_COMMAND);
        if (primaryCommand == null) {
            throw new IllegalStateException("Primary command '/" + PRIMARY_COMMAND + "' is not defined in plugin.yml");
        }
        primaryCommand.setExecutor(registeredExecutor);
        primaryCommand.setTabCompleter(registeredExecutor);
    }

    public static void refreshCommandConfiguration(Main plugin) {
        if (registeredExecutor == null) {
            register(plugin);
            return;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            String action = args[0].toLowerCase(Locale.ENGLISH);
            switch (action) {
                case "help": {
                    if (!hasPermission(sender, PERMISSION_HELP)) {
                        sendNoPermission(sender);
                        break;
                    }
                    for (String line : getHelpLines()) {
                        TextUtils.sendRawMessage(sender, line);
                    }
                    break;
                }
                case "info": {
                    if (!hasPermission(sender, PERMISSION_INFO)) {
                        sendNoPermission(sender);
                        break;
                    }
                    for (String line : getInfoLines()) {
                        TextUtils.sendRawMessage(sender, line);
                    }
                    break;
                }
                case "give": {
                    if (!hasPermission(sender, PERMISSION_GIVE)) {
                        sendNoPermission(sender);
                        break;
                    }
                    if (args.length > 1) {
                        String name = args[1];
                        Player player = Bukkit.getPlayer(name);
                        if (player != null) {
                            player.getInventory().addItem(XMas.XMAS_CRYSTAL);
                        } else {
                            TextUtils.sendMessage(sender, LocaleManager.COMMAND_PLAYER_OFFLINE);
                        }
                    } else {
                        TextUtils.sendMessage(sender, LocaleManager.COMMAND_NO_PLAYER_NAME);
                    }
                    break;
                }
                case "end": {
                    if (!hasPermission(sender, PERMISSION_END)) {
                        sendNoPermission(sender);
                        break;
                    }
                    plugin.end();
                    break;
                }
                case "gifts": {
                    handleGifts(sender, args);
                    break;
                }
                case "reload": {
                    if (!hasPermission(sender, PERMISSION_RELOAD)) {
                        sendNoPermission(sender);
                        break;
                    }
                    Main.ReloadSummary summary = plugin.reloadPluginConfig();
                    TextUtils.sendRawMessage(sender, LocaleManager.text("command.reload-success", TextUtils.success(TextUtils.displayName() + " configuration reloaded.")));
                    for (String line : getReloadSummaryLines(summary)) {
                        TextUtils.sendRawMessage(sender, line);
                    }
                    break;
                }
                case "inspect": {
                    if (!hasPermission(sender, PERMISSION_INSPECT)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleInspect(sender, args);
                    break;
                }
                case "test": {
                    if (!hasPermission(sender, PERMISSION_TEST)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleTest(sender, args);
                    break;
                }
                case "data": {
                    if (!hasPermission(sender, PERMISSION_DATA)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleData(sender, args);
                    break;
                }
                case "journal": {
                    if (!hasPermission(sender, PERMISSION_JOURNAL)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleJournal(sender, args);
                    break;
                }
                case "milestones": {
                    if (!hasPermission(sender, PERMISSION_MILESTONES)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleMilestones(sender, args);
                    break;
                }
                case "community": {
                    if (!hasPermission(sender, PERMISSION_COMMUNITY)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleCommunity(sender, args);
                    break;
                }
                case "addhand": {
                    if (!hasPermission(sender, PERMISSION_ADDHAND)) {
                        sendNoPermission(sender);
                        break;
                    }
                    handleAddHand(sender, commandPath("addhand"));
                    break;
                }
                case "debug": {
                    handleDebug(sender, args);
                    break;
                }

                default:
                    return false;
            }
        } else {
            if (!hasPermission(sender, PERMISSION_STATUS)) {
                sendNoPermission(sender);
            } else {
                sendStatus(sender);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String typed = args[0].toLowerCase(Locale.ENGLISH);
            for (String subCommand : COMMANDS) {
                if (subCommand.startsWith(typed) && canUseSubCommand(sender, subCommand)) {
                    suggestions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String typed = args[1].toLowerCase(Locale.ENGLISH);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(typed)) {
                    suggestions.add(player.getName());
                }
            }
        } else if (args[0].equalsIgnoreCase("inspect")) {
            if (args.length == 2) {
                List<String> inspectSuggestions = new ArrayList<>();
                inspectSuggestions.add("nearest");
                for (MagicTree tree : XMas.getAllTrees()) {
                    inspectSuggestions.add(tree.getTreeUID().toString());
                }
                suggestions.addAll(filterStartingWith(inspectSuggestions, args[1]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("nearest")) {
                String typed = args[2].toLowerCase(Locale.ENGLISH);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(typed)) {
                        suggestions.add(player.getName());
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("test")) {
            if (args.length == 2) {
                suggestions.addAll(filterStartingWith(Arrays.asList("sound", "particle", "refund"), args[1]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("sound")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("first", "repeat"), args[2]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("particle")) {
                suggestions.addAll(filterStartingWith(treeLevelNames(), args[2]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("refund")) {
                List<String> refundSuggestions = new ArrayList<>();
                refundSuggestions.add("nearest");
                for (MagicTree tree : XMas.getAllTrees()) {
                    refundSuggestions.add(tree.getTreeUID().toString());
                }
                suggestions.addAll(filterStartingWith(refundSuggestions, args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("particle")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("all", "ambient", "swag", "body"), args[3]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("refund") && args[2].equalsIgnoreCase("nearest")) {
                String typed = args[3].toLowerCase(Locale.ENGLISH);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(typed)) {
                        suggestions.add(player.getName());
                    }
                }
            } else if ((args.length == 4 && args[1].equalsIgnoreCase("sound")) || (args.length == 5 && args[1].equalsIgnoreCase("particle"))) {
                String typed = args[args.length - 1].toLowerCase(Locale.ENGLISH);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(typed)) {
                        suggestions.add(player.getName());
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("data")) {
            if (args.length == 2) {
                suggestions.addAll(filterStartingWith(Arrays.asList("backup", "validate", "report", "migrate-world"), args[1]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("backup")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("trees", "progress", "all"), args[2]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("migrate-world")) {
                suggestions.addAll(filterStartingWith(getWorldNames(), args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("migrate-world")) {
                suggestions.addAll(filterStartingWith(getWorldNames(), args[3]));
            } else if (args.length == 5 && args[1].equalsIgnoreCase("migrate-world")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("dry-run", "apply"), args[4]));
            }
        } else if (args[0].equalsIgnoreCase("gifts")) {
            if (args.length == 2) {
                suggestions.addAll(filterStartingWith(Arrays.asList("list", "roll", "remove", "weight", "spawn", "addhand"), args[1]));
            } else if (args.length == 3 && (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("weight"))) {
                suggestions.addAll(filterStartingWith(giftIndexes(), args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("weight")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("1", "2", "5", "10", "25", "50", "100"), args[3]));
            }
        } else if (args[0].equalsIgnoreCase("journal")) {
            if (args.length == 2) {
                List<String> journalSuggestions = new ArrayList<>();
                journalSuggestions.add("gui");
                addOnlinePlayerNames(journalSuggestions);
                suggestions.addAll(filterStartingWith(journalSuggestions, args[1]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("gui")) {
                suggestions.addAll(filterStartingWith(onlinePlayerNames(), args[2]));
            }
        } else if (args[0].equalsIgnoreCase("milestones")) {
            if (args.length == 2) {
                List<String> milestoneSuggestions = new ArrayList<>();
                milestoneSuggestions.add("claim");
                milestoneSuggestions.add("test");
                addOnlinePlayerNames(milestoneSuggestions);
                suggestions.addAll(filterStartingWith(milestoneSuggestions, args[1]));
            } else if (args.length == 3 && (args[1].equalsIgnoreCase("claim") || args[1].equalsIgnoreCase("test"))) {
                suggestions.addAll(filterStartingWith(playerMilestoneKeys(), args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("test")) {
                suggestions.addAll(filterStartingWith(onlinePlayerNames(), args[3]));
            }
        } else if (args[0].equalsIgnoreCase("community")) {
            if (args.length == 2) {
                suggestions.addAll(filterStartingWith(Arrays.asList("claim", "test"), args[1]));
            } else if (args.length == 3 && (args[1].equalsIgnoreCase("claim") || args[1].equalsIgnoreCase("test"))) {
                suggestions.addAll(filterStartingWith(communityMilestoneKeys(), args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("claim")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("confirm"), args[3]));
            }
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (args.length == 2) {
                List<String> debugSuggestions = new ArrayList<>(DEBUG_SECTIONS.keySet());
                if (hasPermission(sender, PERMISSION_DEBUG_TOGGLE)) {
                    debugSuggestions.add("toggle");
                }
                suggestions.addAll(filterStartingWith(debugSuggestions, args[1]));
            } else if (args.length == 3 && args[1].equalsIgnoreCase("toggle")) {
                suggestions.addAll(filterStartingWith(new ArrayList<>(DEBUG_TOGGLE_KEYS), args[2]));
            } else if (args.length == 4 && args[1].equalsIgnoreCase("toggle")) {
                suggestions.addAll(filterStartingWith(Arrays.asList("true", "false"), args[3]));
            }
        }
        return suggestions;
    }

    private void handleGifts(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("spawn")) {
            if (!hasPermission(sender, PERMISSION_GIFTS)) {
                sendNoPermission(sender);
                return;
            }
            spawnPresentsUnderTrees();
            return;
        }
        if (args[1].equalsIgnoreCase("addhand")) {
            if (!hasPermission(sender, PERMISSION_ADDHAND)) {
                sendNoPermission(sender);
                return;
            }
            handleAddHand(sender, commandPath("gifts addhand"));
            return;
        }
        if (!hasPermission(sender, PERMISSION_GIFTS)) {
            sendNoPermission(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            int page = parsePositiveInt(args.length >= 3 ? args[2] : null, 1);
            sendGiftList(sender, page);
            return;
        }
        if (args[1].equalsIgnoreCase("roll")) {
            sendGiftRoll(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("remove")) {
            handleGiftRemove(sender, args);
            return;
        }
        if (args[1].equalsIgnoreCase("weight")) {
            handleGiftWeight(sender, args);
            return;
        }

        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.unknown-action", "Unknown Gift Action"), TextUtils.error(args[1])));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.usage", "Usage"), TextUtils.command(commandPath("gifts list"))));
    }

    private void handleAddHand(CommandSender sender, String commandForPrompt) {
        if (!(sender instanceof Player player)) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-only", TextUtils.error("Only players can use this command.")));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            TextUtils.sendRawMessage(player, replaceToken(
                    LocaleManager.text("command.hold-item-first", TextUtils.error("Hold an item before running {command}.")),
                    "command",
                    commandForPrompt
            ));
            return;
        }
        plugin.addGiftItem(item.clone());
        TextUtils.sendRawMessage(player, LocaleManager.text("command.gift-added", TextUtils.success("Added the held item to the gift list.")));
    }

    private void handleJournal(CommandSender sender, String[] args) {
        boolean gui = args.length >= 2 && args[1].equalsIgnoreCase("gui");
        String targetName = gui
                ? (args.length >= 3 ? args[2] : null)
                : (args.length >= 2 ? args[1] : null);
        OfflinePlayer target = resolvePlayerTarget(sender, targetName);
        if (target == null) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-required", TextUtils.error("Player name required.")));
            return;
        }
        if (!canViewTarget(sender, target)) {
            sendNoPermission(sender);
            return;
        }
        if (gui) {
            if (!(sender instanceof Player viewer)) {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-only", TextUtils.error("Only players can use this command.")));
                return;
            }
            openJournalGui(viewer, target);
            return;
        }
        sendJournal(sender, target);
    }

    private void handleMilestones(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("test")) {
            if (!hasPermission(sender, PERMISSION_TEST)) {
                sendNoPermission(sender);
                return;
            }
            if (args.length < 3) {
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.usage", "Usage"), TextUtils.command(commandPath("milestones test <key> [player]"))));
                return;
            }
            OfflinePlayer target = resolvePlayerTarget(sender, args.length >= 4 ? args[3] : null);
            if (target == null) {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-required", TextUtils.error("Player name required.")));
                return;
            }
            sendRewardPreview(sender, plugin.getEventProgress().previewPlayerMilestone(target, args[2], false),
                    commandPath("milestones claim " + args[2]));
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("claim")) {
            if (!hasPermission(sender, PERMISSION_MILESTONES_CLAIM)) {
                sendNoPermission(sender);
                return;
            }
            if (!(sender instanceof Player player)) {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-only", TextUtils.error("Only players can use this command.")));
                return;
            }
            if (args.length < 3) {
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.usage", "Usage"), TextUtils.command(commandPath("milestones claim <key>"))));
                return;
            }
            EventProgress.ClaimResult result = plugin.getEventProgress().claimPlayerMilestone(player, args[2]);
            TextUtils.sendRawMessage(sender, result.success() ? TextUtils.success(result.message()) : TextUtils.error(result.message()));
            return;
        }
        OfflinePlayer target = resolvePlayerTarget(sender, args.length >= 2 ? args[1] : null);
        if (target == null) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.player-required", TextUtils.error("Player name required.")));
            return;
        }
        if (!canViewTarget(sender, target)) {
            sendNoPermission(sender);
            return;
        }
        sendMilestones(sender, target);
    }

    private void handleCommunity(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("test")) {
            if (!hasPermission(sender, PERMISSION_TEST)) {
                sendNoPermission(sender);
                return;
            }
            if (args.length < 3) {
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.community.labels.usage", "Usage"), TextUtils.command(commandPath("community test <key>"))));
                return;
            }
            sendRewardPreview(sender, plugin.getEventProgress().previewCommunityMilestone(sender, args[2], false),
                    commandPath("community claim " + args[2] + " confirm"));
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("claim")) {
            if (!hasPermission(sender, PERMISSION_COMMUNITY_CLAIM)) {
                sendNoPermission(sender);
                return;
            }
            if (args.length < 3) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.community.labels.usage", "Usage"), TextUtils.command(commandPath("community claim <key> confirm"))));
                return;
            }
            if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
                EventProgress.RewardPreview preview = plugin.getEventProgress().previewCommunityMilestone(sender, args[2], true);
                sendRewardPreview(sender, preview, commandPath("community claim " + args[2] + " confirm"));
                if (preview.success()) {
                    TextUtils.sendRawMessage(sender, LocaleManager.text("command.community.confirm-required", TextUtils.warning("Run the confirm command to claim this community milestone.")));
                }
                return;
            }
            EventProgress.ClaimResult result = plugin.getEventProgress().claimCommunityMilestone(sender, args[2]);
            TextUtils.sendRawMessage(sender, result.success() ? TextUtils.success(result.message()) : TextUtils.error(result.message()));
            return;
        }
        sendCommunity(sender);
    }

    private void sendJournal(CommandSender sender, OfflinePlayer target) {
        EventProgress.PlayerStats stats = plugin.getEventProgress().playerStats(target);
        TextUtils.sendRawMessage(sender, LocaleManager.text("ui.journal.title", TextUtils.title("Tree Journal")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.player", "Player"), TextUtils.text(stats.name())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.trees", "Trees"), TextUtils.text(Integer.toString(countOwnerTrees(target.getUniqueId())))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.highest-tree", "Highest Tree"), TextUtils.text(highestTreeLevelName(target.getUniqueId()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.gifts-today", "Gifts Today"), TextUtils.text(Integer.toString(stats.giftsToday()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.total-gifts", "Total Gifts"), TextUtils.text(Integer.toString(stats.totalGifts()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.streak", "Current Streak"), TextUtils.text(stats.currentStreak() + " days")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.best-streak", "Best Streak"), TextUtils.text(stats.bestStreak() + " days")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.claimable", "Claimable Milestones"), TextUtils.text(Integer.toString(stats.claimableMilestones()))));
        if (sender instanceof Player) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.journal.labels.gui", "GUI"), TextUtils.command(commandPath("journal gui"))));
        }
        sendHistoryList(sender,
                LocaleManager.text("ui.journal.labels.tree-history", "Tree History"),
                plugin.getEventProgress().playerTreeHistory(target, 5));
        sendHistoryList(sender,
                LocaleManager.text("ui.journal.labels.milestone-history", "Claimed Milestones"),
                plugin.getEventProgress().playerMilestoneHistory(target, 5));
    }

    private void sendMilestones(CommandSender sender, OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        TextUtils.sendRawMessage(sender, LocaleManager.text("ui.milestones.title", TextUtils.title("Milestones")));
        for (EventProgress.Milestone milestone : plugin.getEventProgress().playerMilestones()) {
            int progress = plugin.getEventProgress().progressValue(uuid, milestone);
            boolean claimed = plugin.getEventProgress().isPlayerMilestoneClaimed(uuid, milestone.key());
            boolean eligible = plugin.getEventProgress().isPlayerMilestoneEligible(uuid, milestone);
            TextUtils.sendRawMessage(sender, formatStyledListEntry(milestone.key(), milestoneLine(milestone, progress, claimed, eligible)));
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.claim", "Claim"), TextUtils.command(commandPath("milestones claim <key>"))));
    }

    private void sendCommunity(CommandSender sender) {
        TextUtils.sendRawMessage(sender, LocaleManager.text("ui.community.title", TextUtils.title("Community Milestones")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.community.labels.gifts-opened", "Gifts Opened"), TextUtils.text(Integer.toString(plugin.getEventProgress().getCommunityGiftClaims()))));
        for (EventProgress.Milestone milestone : plugin.getEventProgress().communityMilestones()) {
            int progress = plugin.getEventProgress().getCommunityGiftClaims();
            boolean claimed = plugin.getEventProgress().isCommunityMilestoneClaimed(milestone.key());
            boolean eligible = plugin.getEventProgress().isCommunityMilestoneEligible(milestone);
            TextUtils.sendRawMessage(sender, formatStyledListEntry(milestone.key(), milestoneLine(milestone, progress, claimed, eligible)));
        }
        if (hasPermission(sender, PERMISSION_COMMUNITY_CLAIM)) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.community.labels.claim", "Claim"), TextUtils.command(commandPath("community claim <key> confirm"))));
        }
    }

    private String milestoneLine(EventProgress.Milestone milestone, int progress, boolean claimed, boolean eligible) {
        String state;
        if (claimed) {
            state = TextUtils.success(LocaleManager.text("ui.milestones.values.claimed", "claimed"));
        } else if (eligible) {
            state = TextUtils.warning(LocaleManager.text("ui.milestones.values.ready", "ready"));
        } else {
            state = TextUtils.text(Math.min(progress, milestone.target()) + " / " + milestone.target());
        }
        return TextUtils.text(milestone.displayName()) + TextUtils.muted(" - ") + state;
    }

    private void openJournalGui(Player viewer, OfflinePlayer target) {
        EventProgress.PlayerStats stats = plugin.getEventProgress().playerStats(target);
        Inventory inventory = Bukkit.createInventory(
                new JournalInventoryHolder(target.getUniqueId()),
                27,
                TextUtils.parse(LocaleManager.text("ui.journal.gui.title", "<xm-accent>Tree Journal</xm-accent>"))
        );
        inventory.setItem(10, journalItem(Material.SPRUCE_SAPLING, LocaleManager.text("ui.journal.gui.trees", "Trees"),
                List.of(
                        formatPlain(LocaleManager.text("ui.journal.labels.trees", "Trees"), countOwnerTrees(target.getUniqueId())),
                        formatPlain(LocaleManager.text("ui.journal.labels.highest-tree", "Highest Tree"), highestTreeLevelName(target.getUniqueId()))
                )));
        inventory.setItem(12, journalItem(Material.CHEST, LocaleManager.text("ui.journal.gui.gifts", "Gifts"),
                List.of(
                        formatPlain(LocaleManager.text("ui.journal.labels.gifts-today", "Gifts Today"), stats.giftsToday()),
                        formatPlain(LocaleManager.text("ui.journal.labels.total-gifts", "Total Gifts"), stats.totalGifts())
                )));
        inventory.setItem(14, journalItem(Material.GOLD_INGOT, LocaleManager.text("ui.journal.gui.streak", "Streak"),
                List.of(
                        formatPlain(LocaleManager.text("ui.journal.labels.streak", "Current Streak"), stats.currentStreak() + " days"),
                        formatPlain(LocaleManager.text("ui.journal.labels.best-streak", "Best Streak"), stats.bestStreak() + " days")
                )));
        inventory.setItem(16, journalItem(Material.NETHER_STAR, LocaleManager.text("ui.journal.gui.milestones", "Milestones"),
                List.of(formatPlain(LocaleManager.text("ui.journal.labels.claimable", "Claimable Milestones"), stats.claimableMilestones()))));
        viewer.openInventory(inventory);
    }

    private ItemStack journalItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtils.parse(TextUtils.title(name)));
            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                formattedLore.add(TextUtils.text(line));
            }
            meta.lore(TextUtils.parseList(formattedLore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void spawnPresentsUnderTrees() {
        Random random = new Random();
        for (MagicTree magicTree : XMas.getAllTrees()) {
            for (int i = 0; i < 3 + random.nextInt(4); i++) {
                magicTree.spawnPresent();
            }
        }
        Bukkit.broadcast(TextUtils.parse(LocaleManager.COMMAND_GIVEAWAY));
    }

    private void sendGiftList(CommandSender sender, int page) {
        List<Main.GiftEntry> giftItems = plugin.getGiftEntries();
        int pageSize = 8;
        int pageCount = Math.max(1, (int) Math.ceil(giftItems.size() / (double) pageSize));
        page = Math.max(1, Math.min(page, pageCount));
        int start = (page - 1) * pageSize;
        int end = Math.min(giftItems.size(), start + pageSize);
        int totalWeight = Main.getGiftTotalWeight();

        TextUtils.sendRawMessage(sender, LocaleManager.text("ui.gifts.title", TextUtils.title("Gift Pool")) + " " + TextUtils.muted("(" + page + "/" + pageCount + ")"));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.total", "Total"), TextUtils.text(Integer.toString(giftItems.size()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.total-weight", "Total Weight"), TextUtils.text(Integer.toString(totalWeight))));
        if (giftItems.isEmpty()) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("ui.gifts.values.empty", TextUtils.warning("No gifts are configured.")));
            return;
        }
        for (int index = start; index < end; index++) {
            TextUtils.sendRawMessage(sender, formatStyledListEntry(Integer.toString(index + 1), formatGiftEntry(giftItems.get(index), totalWeight)));
        }
        if (page < pageCount) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.next", "Next"), TextUtils.command(commandPath("gifts list " + (page + 1)))));
        }
    }

    private void sendGiftRoll(CommandSender sender) {
        ItemStack gift = plugin.rollGiftItem();
        if (gift == null) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("ui.gifts.values.empty", TextUtils.warning("No gifts are configured.")));
            return;
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.rolled", "Rolled"), formatGiftItem(gift)));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.total-weight", "Total Weight"), TextUtils.text(Integer.toString(Main.getGiftTotalWeight()))));
    }

    private void handleGiftRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.usage", "Usage"), TextUtils.command(commandPath("gifts remove <index>"))));
            return;
        }
        int index = parsePositiveInt(args[2], -1);
        if (index < 1) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.index", "Index"), TextUtils.error(args[2])));
            return;
        }
        ItemStack removed = plugin.removeGiftItem(index);
        if (removed == null) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.index", "Index"), TextUtils.error(Integer.toString(index))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.try", "Try"), TextUtils.command(commandPath("gifts list"))));
            return;
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.removed", "Removed"), formatGiftItem(removed)));
    }

    private void handleGiftWeight(CommandSender sender, String[] args) {
        if (args.length < 4) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.usage", "Usage"), TextUtils.command(commandPath("gifts weight <index> <weight>"))));
            return;
        }
        int index = parsePositiveInt(args[2], -1);
        int weight = parsePositiveInt(args[3], -1);
        if (index < 1) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.index", "Index"), TextUtils.error(args[2])));
            return;
        }
        if (weight < 1) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.weight", "Weight"), TextUtils.error(args[3])));
            return;
        }
        Main.GiftEntry updated = plugin.updateGiftWeight(index, weight);
        if (updated == null) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.index", "Index"), TextUtils.error(Integer.toString(index))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.try", "Try"), TextUtils.command(commandPath("gifts list"))));
            return;
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.gifts.labels.updated", "Updated"), formatGiftEntry(updated, Main.getGiftTotalWeight())));
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(commandPath("test sound first|repeat [player]"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(commandPath("test particle <level> [all|ambient|swag|body] [player]"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(commandPath("test refund [tree-uuid|nearest [player]]"))));
            return;
        }

        if (args[1].equalsIgnoreCase("sound")) {
            handleTestSound(sender, args);
            return;
        }
        if (args[1].equalsIgnoreCase("particle")) {
            handleTestParticle(sender, args);
            return;
        }
        if (args[1].equalsIgnoreCase("refund")) {
            handleTestRefund(sender, args);
            return;
        }

        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.unknown-test", "Unknown Test"), TextUtils.error(args[1])));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.try", "Try"), TextUtils.command(commandPath("test sound first"))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.try", "Try"), TextUtils.command(commandPath("test refund nearest"))));
    }

    private void handleTestRefund(CommandSender sender, String[] args) {
        MagicTree tree = resolveTreeForTestRefund(sender, args);
        if (tree == null) {
            return;
        }
        for (String line : getRefundTestLines(tree)) {
            TextUtils.sendRawMessage(sender, line);
        }
    }

    private void handleTestSound(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(commandPath("test sound first|repeat [player]"))));
            return;
        }

        boolean repeat;
        if (args[2].equalsIgnoreCase("first")) {
            repeat = false;
        } else if (args[2].equalsIgnoreCase("repeat")) {
            repeat = true;
        } else {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.sound", "Sound"), TextUtils.error(args[2])));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.try", "Try"), TextUtils.command(commandPath("test sound first"))));
            return;
        }

        Player target = resolveCommandTarget(sender, args, 3, commandPath("test sound first <player>"));
        if (target == null) {
            return;
        }

        float volume = repeat ? Main.growRepeatSoundVolume : Main.growFirstSoundVolume;
        if (volume > 0) {
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, volume, 0.8f);
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.sound", "Sound"), TextUtils.text(repeat ? "repeat" : "first")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.target", "Target"), TextUtils.text(target.getName())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.volume", "Volume"), TextUtils.text(Float.toString(volume))));
    }

    private void handleTestParticle(CommandSender sender, String[] args) {
        if (args.length < 3) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(commandPath("test particle <level> [all|ambient|swag|body] [player]"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.levels", "Levels"), TextUtils.text(String.join(", ", treeLevelNames()))));
            return;
        }

        TreeLevel level = treeLevelByName(args[2]);
        if (level == null) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.level", "Level"), TextUtils.error(args[2])));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.levels", "Levels"), TextUtils.text(String.join(", ", treeLevelNames()))));
            return;
        }

        String effectName = "all";
        int targetArgIndex = 3;
        if (args.length >= 4 && isParticleEffectName(args[3])) {
            effectName = args[3].toLowerCase(Locale.ENGLISH);
            targetArgIndex = 4;
        }

        Player target = resolveCommandTarget(sender, args, targetArgIndex, commandPath("test particle <level> [all|ambient|swag|body] <player>"));
        if (target == null) {
            return;
        }

        int played = playParticlePreview(level, effectName, target.getLocation());
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.particle", "Particle"), TextUtils.text(level.getLevelName() + " " + effectName)));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.target", "Target"), TextUtils.text(target.getName())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.effects-played", "Effects Played"), TextUtils.text(Integer.toString(played))));
        if (played == 0) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.test.no-particles", TextUtils.warning("No enabled particle effects matched that preview.")));
        }
    }

    private void handleData(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data backup"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data backup progress"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data validate"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data report"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data migrate-world <from> <to> [dry-run|apply]"))));
            return;
        }

        if (args[1].equalsIgnoreCase("backup")) {
            handleDataBackup(sender, args.length >= 3 ? args[2] : "trees");
            return;
        }
        if (args[1].equalsIgnoreCase("validate")) {
            handleDataValidate(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("report")) {
            handleDataReport(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("migrate-world")) {
            handleDataMigrateWorld(sender, args);
            return;
        }

        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.unknown-action", "Unknown Data Action"), TextUtils.error(args[1])));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.try", "Try"), TextUtils.command(commandPath("data validate"))));
    }

    private void handleDataReport(CommandSender sender) {
        TreeSerializer.TreeSummary treeSummary = TreeSerializer.summarizeTreesFile();
        EventProgress.EventSummary eventSummary = plugin.getEventProgress().eventSummary();

        TextUtils.sendRawMessage(sender, formatSectionTitle(LocaleManager.text("ui.data.report-title", "Event Wrap-Up Report")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.stored-trees", "Stored Trees"), TextUtils.text(Integer.toString(treeSummary.storedTreeCount()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.loaded-trees", "Loaded Trees"), TextUtils.text(Integer.toString(treeSummary.loadedTreeCount()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.tree-owners", "Tree Owners"), TextUtils.text(Integer.toString(treeSummary.owners().size()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.level-counts", "Level Counts"), TextUtils.text(formatLevelCounts(treeSummary.levelCounts()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.gifts-opened", "Gifts Opened"), TextUtils.text(Integer.toString(eventSummary.communityGiftClaims()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.players-with-gifts", "Players With Gifts"), TextUtils.text(Integer.toString(eventSummary.playersWithGifts()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.claimed-player-milestones", "Claimed Player Milestones"), TextUtils.text(Integer.toString(eventSummary.claimedPlayerMilestones()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.claimed-community-milestones", "Claimed Community Milestones"), TextUtils.text(Integer.toString(eventSummary.claimedCommunityMilestones()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.legacy-trees-preserved", "Legacy Trees Preserved"), TextUtils.text(Integer.toString(treeSummary.storedTreeCount()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.returning-player-reward-list", "Returning-Player Reward List"), TextUtils.text(formatReturningPlayers(treeSummary.owners(), 12))));
        sendHistoryList(sender,
                LocaleManager.text("ui.data.labels.community-milestone-history", "Community Milestone History"),
                plugin.getEventProgress().communityMilestoneHistory(6));
    }

    private void handleDataBackup(CommandSender sender, String mode) {
        if (!mode.equalsIgnoreCase("trees") && !mode.equalsIgnoreCase("progress") && !mode.equalsIgnoreCase("all")) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.mode", "Mode"), TextUtils.error(mode)));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data backup [trees|progress|all]"))));
            return;
        }
        try {
            if (mode.equalsIgnoreCase("all")) {
                File treesBackup = TreeSerializer.backupTreesFile();
                File progressBackup = plugin.getEventProgress().backupProgressFile();
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.data.backup-created", TextUtils.success("Data backup created.")));
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.file", "File"), TextUtils.text(treesBackup.getPath())));
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.file", "File"), TextUtils.text(progressBackup.getPath())));
                return;
            }
            File backupFile = mode.equalsIgnoreCase("progress")
                    ? plugin.getEventProgress().backupProgressFile()
                    : TreeSerializer.backupTreesFile();
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.data.backup-created", TextUtils.success("Data backup created.")));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.file", "File"), TextUtils.text(backupFile.getPath())));
        } catch (IOException exception) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.data.backup-failed", TextUtils.error("Unable to back up data.")));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.error", "Error"), TextUtils.error(exception.getMessage())));
        }
    }

    private void sendRewardPreview(CommandSender sender, EventProgress.RewardPreview preview, String claimCommand) {
        if (!preview.success()) {
            TextUtils.sendRawMessage(sender, TextUtils.error(preview.message()));
            return;
        }
        TextUtils.sendRawMessage(sender, formatSectionTitle(LocaleManager.text("ui.milestones.reward-preview-title", "Milestone Reward Preview")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.milestone", "Milestone"), TextUtils.command(preview.milestone().key())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.display-name", "Display Name"), TextUtils.text(preview.milestone().displayName())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.progress", "Progress"), TextUtils.text(preview.progress() + " / " + preview.milestone().target())));
        if (preview.commands().isEmpty()) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.commands", "Commands"), TextUtils.warning(LocaleManager.text("ui.milestones.values.no-commands", "none configured"))));
        } else {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.commands", "Commands"), TextUtils.text(Integer.toString(preview.commands().size()))));
            int index = 1;
            for (String rewardCommand : preview.commands()) {
                TextUtils.sendRawMessage(sender, formatStyledListEntry(Integer.toString(index), TextUtils.command(rewardCommand)));
                index++;
            }
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.milestones.labels.claim", "Claim"), TextUtils.command(claimCommand)));
    }

    private void handleDataValidate(CommandSender sender) {
        TreeSerializer.TreeDataValidationReport report = TreeSerializer.validateTreesFile();
        TextUtils.sendRawMessage(sender, formatSectionTitle(LocaleManager.text("ui.data.title", "Tree Data Validation")));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.file", "File"), TextUtils.text(TreeSerializer.getTreesFile().getPath())));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.stored-trees", "Stored Trees"), TextUtils.text(Integer.toString(report.storedTreeCount()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.loaded-trees", "Loaded Trees"), TextUtils.text(Integer.toString(report.loadedTreeCount()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.status", "Status"), report.hasWarnings()
                ? LocaleManager.text("ui.data.values.warnings", TextUtils.warning("warnings"))
                : LocaleManager.text("ui.data.values.clean", TextUtils.success("clean"))));
        sendValidationSet(sender, "invalid-tree-ids", report.invalidTreeIds());
        sendValidationSet(sender, "invalid-owners", report.invalidOwners());
        sendValidationSet(sender, "invalid-levels", report.invalidLevels());
        sendValidationSet(sender, "invalid-locations", report.invalidLocations());
        sendValidationSet(sender, "missing-worlds", report.missingWorlds());
        sendValidationSet(sender, "invalid-requirements", report.invalidRequirements());
        sendValidationSet(sender, "duplicate-locations", report.duplicateLocations());
    }

    private void handleDataMigrateWorld(CommandSender sender, String[] args) {
        if (args.length < 4) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data migrate-world <from> <to> [dry-run|apply]"))));
            return;
        }

        String sourceWorld = args[2];
        String targetWorld = args[3];
        boolean apply = args.length >= 5 && args[4].equalsIgnoreCase("apply");
        if (args.length >= 5 && !args[4].equalsIgnoreCase("apply") && !args[4].equalsIgnoreCase("dry-run")) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.mode", "Mode"), TextUtils.error(args[4])));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.usage", "Usage"), TextUtils.command(commandPath("data migrate-world <from> <to> [dry-run|apply]"))));
            return;
        }

        try {
            TreeSerializer.WorldMigrationReport report = TreeSerializer.migrateWorldName(sourceWorld, targetWorld, apply);
            TextUtils.sendRawMessage(sender, formatSectionTitle(LocaleManager.text("ui.data.world-migration-title", "Tree World Migration")));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.source-world", "Source World"), TextUtils.text(report.sourceWorld())));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.target-world", "Target World"), TextUtils.text(report.targetWorld())));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.matched-trees", "Matched Trees"), TextUtils.text(Integer.toString(report.matchedTrees()))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.mode", "Mode"), TextUtils.text(report.applied() ? "apply" : "dry-run")));
            if (report.backupFile() != null) {
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.backup", "Backup"), TextUtils.text(report.backupFile().getPath())));
            }
            if (!report.applied()) {
                TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.apply-command", "Apply Command"), TextUtils.command(commandPath("data migrate-world " + sourceWorld + " " + targetWorld + " apply"))));
            } else {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.data.migration-applied", TextUtils.warning("World migration saved. Restart the server before testing migrated trees.")));
            }
        } catch (IOException exception) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.data.migration-failed", TextUtils.error("Unable to migrate tree world names.")));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.data.labels.error", "Error"), TextUtils.error(exception.getMessage())));
        }
    }

    private void sendValidationSet(CommandSender sender, String labelKey, Set<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        String label = LocaleManager.text("ui.data.labels." + labelKey, labelKey);
        TextUtils.sendRawMessage(sender, formatKeyValue(label, TextUtils.warning(joinLimited(values, 8))));
    }

    private String joinLimited(Collection<String> values, int limit) {
        List<String> limited = new ArrayList<>();
        int index = 0;
        for (String value : values) {
            if (index >= limit) {
                break;
            }
            limited.add(value);
            index++;
        }
        if (values.size() > limit) {
            limited.add("+" + (values.size() - limit) + " more");
        }
        return String.join(", ", limited);
    }

    private void sendHistoryList(CommandSender sender, String title, List<String> history) {
        TextUtils.sendRawMessage(sender, formatSectionTitle(title));
        if (history == null || history.isEmpty()) {
            TextUtils.sendRawMessage(sender, TextUtils.muted(LocaleManager.text("ui.journal.values.no-history", "No history recorded yet.")));
            return;
        }
        for (String entry : history) {
            TextUtils.sendRawMessage(sender, TextUtils.text(entry));
        }
    }

    private String formatLevelCounts(Map<String, Integer> levelCounts) {
        if (levelCounts == null || levelCounts.isEmpty()) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : levelCounts.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join(", ", parts);
    }

    private String formatReturningPlayers(Set<UUID> owners, int limit) {
        if (owners == null || owners.isEmpty()) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        List<String> names = new ArrayList<>();
        for (UUID owner : owners) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            names.add(player.getName() != null ? player.getName() : owner.toString());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return joinLimited(names, limit);
    }

    private int parsePositiveInt(String rawValue, int fallback) {
        if (rawValue == null || rawValue.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private Player resolveCommandTarget(CommandSender sender, String[] args, int targetArgIndex, String consoleUsage) {
        if (args.length > targetArgIndex) {
            Player target = Bukkit.getPlayer(args[targetArgIndex]);
            if (target == null) {
                TextUtils.sendMessage(sender, LocaleManager.COMMAND_PLAYER_OFFLINE);
            }
            return target;
        }
        if (sender instanceof Player player) {
            return player;
        }
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.test.labels.usage", "Usage"), TextUtils.command(consoleUsage)));
        return null;
    }

    private int playParticlePreview(TreeLevel level, String effectName, Location location) {
        int played = 0;
        if ((effectName.equals("all") || effectName.equals("ambient")) && level.getAmbientEffect() != null) {
            level.getAmbientEffect().playEffect(location);
            played++;
        }
        if ((effectName.equals("all") || effectName.equals("swag")) && level.getSwagEffect() != null) {
            level.getSwagEffect().playEffect(location);
            played++;
        }
        if ((effectName.equals("all") || effectName.equals("body")) && level.getBodyEffect() != null) {
            level.getBodyEffect().playEffect(location);
            played++;
        }
        return played;
    }

    private boolean isParticleEffectName(String value) {
        return value != null
                && (value.equalsIgnoreCase("all")
                || value.equalsIgnoreCase("ambient")
                || value.equalsIgnoreCase("swag")
                || value.equalsIgnoreCase("body"));
    }

    private TreeLevel treeLevelByName(String levelName) {
        try {
            return TreeLevel.fromString(levelName);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private List<String> treeLevelNames() {
        return Arrays.asList("sapling", "small_tree", "tree", "magic_tree", "super_tree");
    }

    private MagicTree resolveTreeForTestRefund(CommandSender sender, String[] args) {
        if (args.length >= 3 && args[2].equalsIgnoreCase("nearest")) {
            Player target;
            if (args.length >= 4) {
                target = Bukkit.getPlayer(args[3]);
                if (target == null) {
                    TextUtils.sendMessage(sender, LocaleManager.COMMAND_PLAYER_OFFLINE);
                    return null;
                }
            } else if (sender instanceof Player player) {
                target = player;
            } else {
                TextUtils.sendRawMessage(sender, formatKeyValue(
                        LocaleManager.text("ui.test.labels.usage", "Usage"),
                        TextUtils.command(commandPath("test refund nearest <player>"))
                ));
                return null;
            }
            MagicTree tree = findNearestTree(target.getLocation(), 16);
            if (tree == null) {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.inspect.not-found", TextUtils.warning("No XMas Tree found nearby or in your line of sight.")));
            }
            return tree;
        }

        if (args.length >= 3) {
            MagicTree tree = findTreeByUuid(args[2]);
            if (tree == null) {
                TextUtils.sendRawMessage(sender, formatKeyValue(
                        LocaleManager.text("ui.inspect.labels.requested", "Requested"),
                        TextUtils.error(args[2])
                ));
                TextUtils.sendRawMessage(sender, formatKeyValue(
                        LocaleManager.text("ui.inspect.labels.try", "Try"),
                        TextUtils.command(commandPath("test refund nearest"))
                ));
            }
            return tree;
        }

        if (sender instanceof Player player) {
            MagicTree tree = findTargetTree(player);
            if (tree == null) {
                tree = findNearestTree(player.getLocation(), 8);
            }
            if (tree == null) {
                TextUtils.sendRawMessage(sender, LocaleManager.text("command.inspect.not-found", TextUtils.warning("No XMas Tree found nearby or in your line of sight.")));
            }
            return tree;
        }

        TextUtils.sendRawMessage(sender, formatKeyValue(
                LocaleManager.text("ui.test.labels.usage", "Usage"),
                TextUtils.command(commandPath("test refund <tree-uuid>"))
        ));
        TextUtils.sendRawMessage(sender, formatKeyValue(
                LocaleManager.text("ui.test.labels.usage", "Usage"),
                TextUtils.command(commandPath("test refund nearest <player>"))
        ));
        return null;
    }

    private void handleInspect(CommandSender sender, String[] args) {
        MagicTree tree = null;
        if (args.length >= 2 && !args[1].equalsIgnoreCase("nearest")) {
            tree = findTreeByUuid(args[1]);
            if (tree == null) {
                TextUtils.sendRawMessage(sender, formatKeyValue(
                        LocaleManager.text("ui.inspect.labels.requested", "Requested"),
                        TextUtils.error(args[1])
                ));
                TextUtils.sendRawMessage(sender, formatKeyValue(
                        LocaleManager.text("ui.inspect.labels.try", "Try"),
                        TextUtils.command(commandPath("inspect nearest"))
                ));
                return;
            }
        } else if (args.length >= 3 && args[1].equalsIgnoreCase("nearest")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                TextUtils.sendMessage(sender, LocaleManager.COMMAND_PLAYER_OFFLINE);
                return;
            }
            tree = findNearestTree(target.getLocation(), 16);
        } else if (sender instanceof Player player) {
            tree = findTargetTree(player);
            if (tree == null) {
                tree = findNearestTree(player.getLocation(), 8);
            }
        } else {
            TextUtils.sendRawMessage(sender, formatKeyValue(
                    LocaleManager.text("ui.inspect.labels.usage", "Usage"),
                    TextUtils.command(commandPath("inspect <tree-uuid>"))
            ));
            return;
        }

        if (tree == null) {
            TextUtils.sendRawMessage(sender, LocaleManager.text("command.inspect.not-found", TextUtils.warning("No XMas Tree found nearby or in your line of sight.")));
            return;
        }
        for (String line : getInspectLines(tree)) {
            TextUtils.sendRawMessage(sender, line);
        }
    }

    private MagicTree findTreeByUuid(String rawUuid) {
        try {
            return XMas.getTree(UUID.fromString(rawUuid));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private MagicTree findTargetTree(Player player) {
        Block targetBlock = player.getTargetBlockExact(8);
        return MagicTree.getTreeByBlock(targetBlock);
    }

    private MagicTree findNearestTree(Location location, double range) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        double maxDistanceSquared = range * range;
        MagicTree nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (MagicTree tree : XMas.getAllTrees()) {
            Location treeLocation = tree.getLocation();
            if (treeLocation.getWorld() == null || !treeLocation.getWorld().equals(location.getWorld())) {
                continue;
            }
            double distanceSquared = treeLocation.distanceSquared(location);
            if (distanceSquared <= maxDistanceSquared && distanceSquared < nearestDistanceSquared) {
                nearest = tree;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest;
    }

    private List<String> getInspectLines(MagicTree tree) {
        List<String> lines = new ArrayList<>();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(tree.getOwner());
        Location location = tree.getLocation();
        String ownerName = owner.getName() != null ? owner.getName() : LocaleManager.text("ui.inspect.values.unknown-owner", "unknown");

        lines.add(LocaleManager.text("ui.inspect.title", TextUtils.title(TextUtils.displayName() + " Tree Inspect")));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.owner", "Owner"), TextUtils.text(ownerName) + TextUtils.muted(" (" + tree.getOwner() + ")")));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.tree-id", "Tree ID"), TextUtils.text(tree.getTreeUID().toString())));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.owner-online", "Owner Online"), TextUtils.booleanValue(owner.isOnline())));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.owner-loaded-trees", "Owner Loaded Trees"), TextUtils.text(Integer.toString(countOwnerTrees(tree.getOwner())))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.level", "Level"), TextUtils.text(tree.getLevel().getLevelName())));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.next-level", "Next Level"), TextUtils.text(formatNextLevel(tree))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.location", "Location"), TextUtils.text(formatLocation(location))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.world-loaded", "World Loaded"), TextUtils.booleanValue(location != null && location.getWorld() != null)));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.chunk-loaded", "Chunk Loaded"), TextUtils.booleanValue(isChunkLoaded(location))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.tree-built", "Tree Built"), TextUtils.booleanValue(tree.isBuilt())));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.built-blocks", "Built Blocks"), TextUtils.text(Integer.toString(tree.getBuiltBlockCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.can-level-up", "Can Level Up"), TextUtils.booleanValue(tree.canLevelUp())));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.next-space-clear", "Next Space Clear"), TextUtils.booleanValue(canNextLevelFit(tree))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.gift-cooldown", "Gift Cooldown"), TextUtils.text(formatTicks(tree.getLevel().getGiftDelay()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.present-timer", "Present Timer"), TextUtils.text(formatTicks(tree.getPresentCounter()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.scheduled-presents", "Scheduled Presents"), TextUtils.text(Integer.toString(tree.getScheduledPresents()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.nearby-presents", "Nearby Presents"), TextUtils.text(Integer.toString(tree.getNearbyPresentCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.requirement-progress", "Requirement Progress"), TextUtils.text(formatRequirementProgress(tree))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.remaining", "Remaining Requirements"), TextUtils.text(formatRequirements(tree.getLevelupRequirements()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.spent-resources", "Spent Resources"), TextUtils.text(formatSpentResources(tree))));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.resource-back", "Resource Back"), TextUtils.booleanValue(Main.resourceBack)));
        lines.add(formatKeyValue(LocaleManager.text("ui.inspect.labels.refund-preview", "Refund Preview"), TextUtils.text(formatItems(tree.getRefundPreviewItems()))));
        return lines;
    }

    private List<String> getRefundTestLines(MagicTree tree) {
        List<String> lines = new ArrayList<>();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(tree.getOwner());
        Location location = tree.getLocation();
        String ownerName = owner.getName() != null ? owner.getName() : LocaleManager.text("ui.inspect.values.unknown-owner", "unknown");

        lines.add(formatSectionTitle(LocaleManager.text("ui.test.refund-title", "Refund Delivery Dry Run")));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.tree-id", "Tree ID"), TextUtils.text(tree.getTreeUID().toString())));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.owner", "Owner"), TextUtils.text(ownerName) + TextUtils.muted(" (" + tree.getOwner() + ")")));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.location", "Location"), TextUtils.text(formatLocation(location))));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.resource-back", "Resource Back"), TextUtils.booleanValue(Main.resourceBack)));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.refund-items", "Refund Items"), TextUtils.text(formatItems(tree.getRefundPreviewItems()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.delivery-order", "Delivery Order"), TextUtils.text(LocaleManager.text("ui.test.values.delivery-order", "chest -> barrel -> player inventory -> floor drops"))));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.mode", "Mode"), TextUtils.warning(LocaleManager.text("ui.test.values.dry-run", "dry-run"))));
        lines.add(formatKeyValue(LocaleManager.text("ui.test.labels.no-changes", "No Changes"), TextUtils.info(LocaleManager.text("ui.test.values.safe-preview", "This command only previews refund delivery. It does not edit blocks, inventories, tree data, or config."))));
        return lines;
    }

    private List<String> getReloadSummaryLines(Main.ReloadSummary summary) {
        List<String> lines = new ArrayList<>();
        lines.add(formatSectionTitle(LocaleManager.text("ui.reload.title", "Reload Summary")));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.locale", "Locale"), TextUtils.text(summary.locale())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.gifts", "Gifts"), TextUtils.text(Integer.toString(summary.giftCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.present-heads", "Present Heads"), TextUtils.text(Integer.toString(summary.presentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.configured-present-heads", "Configured Present Heads"), TextUtils.text(Integer.toString(summary.configuredPresentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.texture-heads", "Texture Heads"), TextUtils.text(Integer.toString(summary.texturePresentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.legacy-player-heads", "Legacy Player Heads"), warningCount(summary.legacyPlayerHeadCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.invalid-present-heads", "Invalid Present Heads"), warningCount(summary.invalidPresentHeadCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.trees", "Trees"), TextUtils.text(Integer.toString(summary.treeCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.owners", "Owners"), TextUtils.text(Integer.toString(summary.treeOwnerCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.particles", "Particles"), TextUtils.booleanValue(summary.particlesEnabled())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.resource-back", "Resource Back"), TextUtils.booleanValue(summary.resourceBack())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.sounds", "Sounds"), TextUtils.text("first=" + summary.growFirstSoundVolume() + ", repeat=" + summary.growRepeatSoundVolume())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.invalid-gifts", "Invalid Gifts"), warningCount(summary.invalidGiftCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.tree-data-warnings", "Tree Data Warnings"), warningCount(summary.treeDataWarningCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.reload.labels.warnings", "Warnings"), warningCount(summary.configWarningCount())));
        for (String warning : summary.warnings()) {
            lines.add(formatStyledListEntry(LocaleManager.text("ui.reload.labels.warning", "Warning"), TextUtils.warning(warning)));
        }
        return lines;
    }

    private List<String> getDiagnosticsLines(Main.ConfigDiagnostics diagnostics) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(formatSectionTitle(debugSectionDisplayName("diagnostics")));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.configured-present-heads", "Configured Present Heads"), TextUtils.text(Integer.toString(diagnostics.configuredPresentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.valid-present-heads", "Valid Present Heads"), TextUtils.text(Integer.toString(diagnostics.validPresentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.texture-heads", "Texture URL Heads"), TextUtils.text(Integer.toString(diagnostics.texturePresentHeadCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.legacy-player-heads", "Legacy Player Heads"), warningCount(diagnostics.legacyPlayerHeadCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.invalid-present-heads", "Invalid Present Heads"), warningCount(diagnostics.invalidPresentHeadCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.configured-gifts", "Configured Gifts"), TextUtils.text(Integer.toString(diagnostics.configuredGiftCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.valid-gifts", "Valid Gifts"), TextUtils.text(Integer.toString(diagnostics.validGiftCount()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.invalid-gifts", "Invalid Gifts"), warningCount(diagnostics.invalidGiftCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.tree-data-warnings", "Tree Data Warnings"), warningCount(diagnostics.treeDataWarningCount())));
        lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.warning-count", "Warning Count"), warningCount(diagnostics.configWarningCount())));
        if (diagnostics.warnings().isEmpty()) {
            lines.add(formatKeyValue(LocaleManager.text("ui.diagnostics.labels.status", "Status"), LocaleManager.text("ui.data.values.clean", TextUtils.success("clean"))));
        } else {
            for (String warning : diagnostics.warnings()) {
                lines.add(formatStyledListEntry(LocaleManager.text("ui.diagnostics.labels.warning", "Warning"), TextUtils.warning(warning)));
            }
        }
        return lines;
    }

    private void sendStatus(CommandSender sender) {
        for (String line : getStatusLines()) {
            TextUtils.sendRawMessage(sender, line);
        }
    }

    private List<String> getStatusLines() {
        int treeCount = XMas.getAllTrees().size();
        Set<UUID> owners = new HashSet<>();
        for (MagicTree magicTree : XMas.getAllTrees()) {
            owners.add(magicTree.getOwner());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");
        List<String> lines = new ArrayList<>();

        lines.add(
                replaceToken(
                        LocaleManager.text("ui.status.title", TextUtils.title(TextUtils.displayName()) + " " + TextUtils.text("{version}") + " " + TextUtils.title("Plugin Status")),
                        "version",
                        plugin.getPluginMeta().getVersion()
                )
        );
        lines.add("");
        lines.add(formatKeyValue(
                LocaleManager.text("ui.status.labels.event-status", "Event Status"),
                Main.inProgress
                        ? LocaleManager.text("ui.status.values.in-progress", TextUtils.success("In Progress"))
                        : LocaleManager.text("ui.status.values.holidays-end", TextUtils.error("Holidays End"))
        ));
        if (Main.inProgress) {
            lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.current-time", "Current Time"), TextUtils.text(sdf.format(System.currentTimeMillis()))));
            lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.holidays-end", "Holidays End"), TextUtils.text(sdf.format(Main.endTime))));
        }
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.auto-end", "Auto-End"), TextUtils.booleanValue(Main.autoEnd)));
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.resource-back", "Resource Back"), TextUtils.booleanValue(Main.resourceBack)));
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.particles", "Particles"), TextUtils.booleanValue(Main.particlesEnabled)));
        lines.add("");
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.loaded-trees", "Loaded Trees"), TextUtils.text(Integer.toString(treeCount))));
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.tree-owners", "Tree Owners"), TextUtils.text(Integer.toString(owners.size()))));
        lines.add(formatKeyValue(LocaleManager.text("ui.status.labels.help", "Help"), TextUtils.command(commandPath("help"))));
        return lines;
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("toggle")) {
            if (!hasPermission(sender, PERMISSION_DEBUG_TOGGLE)) {
                sendNoPermission(sender);
                return;
            }
            handleDebugToggle(sender, args);
            return;
        }

        if (!hasPermission(sender, PERMISSION_DEBUG)) {
            sendNoPermission(sender);
            return;
        }

        if (args.length < 2) {
            sendDebugSection(sender, "status");
            return;
        }

        String requested = args[1];
        try {
            int page = Integer.parseInt(requested);
            sendDebugPage(sender, page);
            return;
        } catch (NumberFormatException ignored) {
        }

        String section = normalizeDebugSection(requested);
        if (section == null) {
            sendInvalidDebugSelection(sender, requested, DEBUG_SECTIONS.size());
            return;
        }
        sendDebugSection(sender, section);
    }

    private void handleDebugToggle(CommandSender sender, String[] args) {
        if (args.length < 4) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.usage", "Usage"), TextUtils.command(commandPath("debug toggle <key> true|false"))));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.keys", "Keys"), TextUtils.text(String.join(", ", DEBUG_TOGGLE_KEYS))));
            return;
        }

        String key = args[2].toLowerCase(Locale.ENGLISH);
        if (!DEBUG_TOGGLE_KEYS.contains(key)) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.unknown-toggle-key", "Unknown Toggle Key"), TextUtils.error(args[2])));
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.keys", "Keys"), TextUtils.text(String.join(", ", DEBUG_TOGGLE_KEYS))));
            return;
        }
        if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.value", "Value"), LocaleManager.text("ui.debug.invalid-boolean", TextUtils.error("must be true or false"))));
            return;
        }

        boolean value = Boolean.parseBoolean(args[3]);
        plugin.getConfig().set(key, value);
        plugin.saveConfig();
        plugin.reloadPluginConfig();
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.updated", "Updated"), TextUtils.command(key) + TextUtils.muted(" -> ") + TextUtils.booleanValue(value)));
    }

    private LinkedHashMap<String, List<String>> buildDebugSections() {
        LinkedHashMap<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("status", getStatusLines());

        List<String> commandsPage = new ArrayList<>();
        commandsPage.add("");
        commandsPage.add(formatSectionTitle(debugSectionDisplayName("commands")));
        commandsPage.add(formatListEntry(commandPath(""), LocaleManager.text("ui.command-descriptions.status", "status")));
        commandsPage.add(formatListEntry(commandPath("help"), LocaleManager.text("ui.command-descriptions.help", "command list")));
        commandsPage.add(formatListEntry(commandPath("info"), LocaleManager.text("ui.command-descriptions.info", "how the XMas Tree event works")));
        commandsPage.add(formatListEntry(commandPath("give <player>"), LocaleManager.text("ui.command-descriptions.give", "give a Christmas Crystal")));
        commandsPage.add(formatListEntry(commandPath("gifts"), LocaleManager.text("ui.command-descriptions.gifts", "spawn presents under all trees")));
        commandsPage.add(formatListEntry(commandPath("gifts list [page]"), LocaleManager.text("ui.command-descriptions.gifts-list", "list configured gifts")));
        commandsPage.add(formatListEntry(commandPath("gifts roll"), LocaleManager.text("ui.command-descriptions.gifts-roll", "preview a weighted random gift roll")));
        commandsPage.add(formatListEntry(commandPath("gifts remove <index>"), LocaleManager.text("ui.command-descriptions.gifts-remove", "remove a configured gift")));
        commandsPage.add(formatListEntry(commandPath("gifts weight <index> <weight>"), LocaleManager.text("ui.command-descriptions.gifts-weight", "set a gift roll weight")));
        commandsPage.add(formatListEntry(commandPath("gifts addhand"), LocaleManager.text("ui.command-descriptions.gifts-addhand", "add held item to gifts")));
        commandsPage.add(formatListEntry(commandPath("addhand"), LocaleManager.text("ui.command-descriptions.addhand", "legacy shortcut for gifts addhand")));
        commandsPage.add(formatListEntry(commandPath("reload"), LocaleManager.text("ui.command-descriptions.reload", "reload config and locale")));
        commandsPage.add(formatListEntry(commandPath("inspect"), LocaleManager.text("ui.command-descriptions.inspect", "inspect the tree you are looking at")));
        commandsPage.add(formatListEntry(commandPath("test sound first|repeat [player]"), LocaleManager.text("ui.command-descriptions.test-sound", "preview grow sounds")));
        commandsPage.add(formatListEntry(commandPath("test particle <level> [effect] [player]"), LocaleManager.text("ui.command-descriptions.test-particle", "preview configured particles")));
        commandsPage.add(formatListEntry(commandPath("test refund [tree-uuid|nearest [player]]"), LocaleManager.text("ui.command-descriptions.test-refund", "dry-run tree refund delivery")));
        commandsPage.add(formatListEntry(commandPath("data backup [trees|progress|all]"), LocaleManager.text("ui.command-descriptions.data-backup", "back up event data")));
        commandsPage.add(formatListEntry(commandPath("data validate"), LocaleManager.text("ui.command-descriptions.data-validate", "validate trees.yml")));
        commandsPage.add(formatListEntry(commandPath("data report"), LocaleManager.text("ui.command-descriptions.data-report", "show event wrap-up report")));
        commandsPage.add(formatListEntry(commandPath("data migrate-world <from> <to> [dry-run|apply]"), LocaleManager.text("ui.command-descriptions.data-migrate-world", "migrate saved tree world names")));
        commandsPage.add(formatListEntry(commandPath("journal [player]"), LocaleManager.text("ui.command-descriptions.journal", "view tree journal and event progress")));
        commandsPage.add(formatListEntry(commandPath("journal gui [player]"), LocaleManager.text("ui.command-descriptions.journal-gui", "open the tree journal GUI")));
        commandsPage.add(formatListEntry(commandPath("milestones [player]"), LocaleManager.text("ui.command-descriptions.milestones", "view personal milestones")));
        commandsPage.add(formatListEntry(commandPath("milestones test <key> [player]"), LocaleManager.text("ui.command-descriptions.milestones-test", "preview personal milestone reward commands")));
        commandsPage.add(formatListEntry(commandPath("milestones claim <key>"), LocaleManager.text("ui.command-descriptions.milestones-claim", "claim a personal milestone reward")));
        commandsPage.add(formatListEntry(commandPath("community"), LocaleManager.text("ui.command-descriptions.community", "view community milestone progress")));
        commandsPage.add(formatListEntry(commandPath("community test <key>"), LocaleManager.text("ui.command-descriptions.community-test", "preview community milestone reward commands")));
        commandsPage.add(formatListEntry(commandPath("community claim <key> confirm"), LocaleManager.text("ui.command-descriptions.community-claim", "claim a community milestone reward")));
        commandsPage.add(formatListEntry(commandPath("end"), LocaleManager.text("ui.command-descriptions.end", "end the event")));
        commandsPage.add(formatListEntry(commandPath("debug"), LocaleManager.text("ui.command-descriptions.debug", "open the status debug section")));
        commandsPage.add(formatListEntry(commandPath("debug [section|page]"), LocaleManager.text("ui.command-descriptions.debug-section", "extended debug output by category")));
        commandsPage.add(formatListEntry(commandPath("debug toggle <key> true|false"), LocaleManager.text("ui.command-descriptions.debug-toggle", "toggle global booleans")));
        sections.put("commands", commandsPage);

        List<String> permissionsPage = new ArrayList<>();
        permissionsPage.add("");
        permissionsPage.add(formatSectionTitle(debugSectionDisplayName("permissions")));
        for (Map.Entry<String, String> permission : createPermissionDescriptions().entrySet()) {
            permissionsPage.add(formatListEntry(permission.getKey(), permission.getValue()));
        }
        sections.put("permissions", permissionsPage);

        List<String> placeholdersPage = new ArrayList<>();
        placeholdersPage.add("");
        placeholdersPage.add(formatSectionTitle(debugSectionDisplayName("placeholders")));
        placeholdersPage.add(formatKeyValue(
                LocaleManager.text("ui.debug.labels.notes", "Notes"),
                LocaleManager.text("ui.debug.notes.placeholders", TextUtils.text("Requires PlaceholderAPI. Use '_' after prefix, then dotted keys."))
        ));
        Map<String, String> placeholderDescriptions = XMasPlaceholders.descriptions();
        for (String placeholder : XMasPlaceholders.EXAMPLES) {
            placeholdersPage.add(formatListEntry(placeholder, placeholderDescriptions.getOrDefault(placeholder, LocaleManager.text("ui.debug.placeholder-default-description", "registered placeholder"))));
        }
        sections.put("placeholders", placeholdersPage);

        List<String> togglesPage = new ArrayList<>();
        togglesPage.add("");
        togglesPage.add(formatSectionTitle(debugSectionDisplayName("config")));
        for (String key : DEBUG_TOGGLE_KEYS) {
            togglesPage.add(formatKeyValue(key, TextUtils.booleanValue(plugin.getConfig().getBoolean(key))));
        }
        sections.put("config", togglesPage);
        sections.put("diagnostics", getDiagnosticsLines(plugin.getConfigDiagnostics()));

        return sections;
    }

    private String normalizeDebugSection(String requested) {
        if (requested == null || requested.isBlank()) {
            return "status";
        }
        return switch (requested.toLowerCase(Locale.ENGLISH)) {
            case "status" -> "status";
            case "commands", "command" -> "commands";
            case "permissions", "permission", "perms", "perm" -> "permissions";
            case "placeholders", "placeholder", "papi" -> "placeholders";
            case "config", "configuration", "cfg", "toggles" -> "config";
            case "diagnostics", "diagnostic", "diag", "warnings" -> "diagnostics";
            default -> null;
        };
    }

    private void sendDebugSection(CommandSender sender, String sectionKey) {
        LinkedHashMap<String, List<String>> sections = buildDebugSections();
        if (!sections.containsKey(sectionKey)) {
            sendInvalidDebugSelection(sender, sectionKey, sections.size());
            return;
        }
        renderDebugSection(sender, sectionKey, sections);
    }

    private void sendDebugPage(CommandSender sender, int requestedPage) {
        LinkedHashMap<String, List<String>> sections = buildDebugSections();
        List<String> sectionKeys = new ArrayList<>(sections.keySet());
        if (requestedPage < 1 || requestedPage > sectionKeys.size()) {
            sendInvalidDebugSelection(sender, Integer.toString(requestedPage), sectionKeys.size());
            return;
        }
        renderDebugSection(sender, sectionKeys.get(requestedPage - 1), sections);
    }

    private void renderDebugSection(CommandSender sender, String sectionKey, LinkedHashMap<String, List<String>> sections) {
        List<String> sectionKeys = new ArrayList<>(sections.keySet());
        int sectionIndex = sectionKeys.indexOf(sectionKey);
        int page = sectionIndex + 1;
        int pageCount = sectionKeys.size();

        TextUtils.sendRawMessage(sender,
                LocaleManager.text("ui.debug.section-title", TextUtils.title(TextUtils.displayName() + " Debug"))
                        + " "
                        + TextUtils.text(debugSectionDisplayName(sectionKey))
                        + TextUtils.muted(" (" + page + "/" + pageCount + ")"));
        for (String line : sections.get(sectionKey)) {
            TextUtils.sendRawMessage(sender, line);
        }
        if (page < pageCount) {
            String nextSection = sectionKeys.get(sectionIndex + 1);
            TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.next", "Next"), TextUtils.command(commandPath("debug " + nextSection))));
        }
    }

    private void sendInvalidDebugSelection(CommandSender sender, String requested, int pageCount) {
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.sections", "Debug Sections"), TextUtils.text(String.join(", ", DEBUG_SECTIONS.keySet()))));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.pages", "Debug Pages"), TextUtils.text("1-" + pageCount)));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.requested", "Requested"), TextUtils.error(requested)));
        TextUtils.sendRawMessage(sender, formatKeyValue(LocaleManager.text("ui.debug.labels.try", "Try"), TextUtils.command(commandPath("debug status"))));
    }

    private List<String> getHelpLines() {
        List<String> lines = new ArrayList<>();
        for (String line : LocaleManager.COMMAND_HELP) {
            lines.add(line.replaceAll("/" + RETIRED_LEGACY_COMMAND + "(?![A-Za-z])", "/" + PRIMARY_COMMAND));
        }
        addMissingBundledHelpLines(lines);
        return lines;
    }

    private List<String> getInfoLines() {
        List<String> lines = LocaleManager.textList("command.info");
        if (!lines.isEmpty()) {
            return lines;
        }
        return Arrays.asList(
                TextUtils.title(TextUtils.displayName() + " Event Info"),
                TextUtils.text("Get a Christmas Crystal, place a spruce sapling, then right-click the sapling with the crystal."),
                TextUtils.text("Right-click the tree with the listed ingredients to grow it through multiple levels."),
                TextUtils.text("Grown trees spawn gifts below them. Right-click gifts to claim rewards."),
                TextUtils.text("After the event, break the tree log to reclaim the ingredients you spent."),
                TextUtils.text("Claim daily, weekly, and full-event gifts to build streaks, unlock milestones, and help community progress.")
        );
    }

    private String commandPath(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return "/" + PRIMARY_COMMAND;
        }
        return "/" + PRIMARY_COMMAND + " " + suffix;
    }

    private void addMissingBundledHelpLines(List<String> lines) {
        List<String> bundledLines = LocaleManager.bundledTextList("command.help");
        for (String bundledLine : bundledLines) {
            String normalizedLine = bundledLine.replaceAll("/" + RETIRED_LEGACY_COMMAND + "(?![A-Za-z])", "/" + PRIMARY_COMMAND);
            String commandNeedle = findCommandNeedle(normalizedLine);
            if (commandNeedle != null && lines.stream().noneMatch(line -> line.contains(commandNeedle))) {
                lines.add(normalizedLine);
            }
        }
    }

    private String findCommandNeedle(String line) {
        List<String> commandNeedles = List.of(
                commandPath("debug toggle"),
                commandPath("community claim"),
                commandPath("community test"),
                commandPath("community"),
                commandPath("milestones claim"),
                commandPath("milestones test"),
                commandPath("milestones"),
                commandPath("journal gui"),
                commandPath("journal"),
                commandPath("debug status"),
                commandPath("debug"),
                commandPath("test refund"),
                commandPath("test particle"),
                commandPath("test sound"),
                commandPath("data validate"),
                commandPath("data report"),
                commandPath("data migrate-world"),
                commandPath("data backup progress"),
                commandPath("data backup all"),
                commandPath("data backup"),
                commandPath("gifts remove"),
                commandPath("gifts weight"),
                commandPath("gifts roll"),
                commandPath("gifts addhand"),
                commandPath("gifts list"),
                commandPath("inspect"),
                commandPath("reload"),
                commandPath("addhand"),
                commandPath("gifts"),
                commandPath("give"),
                commandPath("help"),
                commandPath("info"),
                commandPath("end"),
                commandPath("")
        );
        for (String commandNeedle : commandNeedles) {
            if (line.contains(commandNeedle)) {
                return commandNeedle;
            }
        }
        return null;
    }

    public static boolean canOverrideTree(CommandSender sender) {
        return hasPermission(sender, PERMISSION_TREE_OVERRIDE);
    }

    private List<String> filterStartingWith(List<String> values, String typed) {
        String lower = typed.toLowerCase(Locale.ENGLISH);
        List<String> matches = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ENGLISH).startsWith(lower)) {
                matches.add(value);
            }
        }
        return matches;
    }

    private boolean canUseSubCommand(CommandSender sender, String subCommand) {
        return switch (subCommand.toLowerCase(Locale.ENGLISH)) {
            case "help" -> hasPermission(sender, PERMISSION_HELP);
            case "info" -> hasPermission(sender, PERMISSION_INFO);
            case "give" -> hasPermission(sender, PERMISSION_GIVE);
            case "end" -> hasPermission(sender, PERMISSION_END);
            case "gifts" -> hasPermission(sender, PERMISSION_GIFTS) || hasPermission(sender, PERMISSION_ADDHAND);
            case "reload" -> hasPermission(sender, PERMISSION_RELOAD);
            case "addhand" -> hasPermission(sender, PERMISSION_ADDHAND);
            case "inspect" -> hasPermission(sender, PERMISSION_INSPECT);
            case "test" -> hasPermission(sender, PERMISSION_TEST);
            case "data" -> hasPermission(sender, PERMISSION_DATA);
            case "journal" -> hasPermission(sender, PERMISSION_JOURNAL);
            case "milestones" -> hasPermission(sender, PERMISSION_MILESTONES);
            case "community" -> hasPermission(sender, PERMISSION_COMMUNITY);
            case "debug" -> hasPermission(sender, PERMISSION_DEBUG) || hasPermission(sender, PERMISSION_DEBUG_TOGGLE);
            default -> false;
        };
    }

    private static boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(PERMISSION_ADMIN) || sender.hasPermission(permission);
    }

    private void sendNoPermission(CommandSender sender) {
        TextUtils.sendRawMessage(sender, LocaleManager.text("command.no-permission", TextUtils.error("You do not have permission to use this command.")));
    }

    private String formatSectionTitle(String title) {
        return TextUtils.title(title);
    }

    private String formatListEntry(String key, String value) {
        return TextUtils.command(key) + TextUtils.muted(" : ") + TextUtils.text(value);
    }

    private String formatStyledListEntry(String key, String value) {
        return TextUtils.command(key) + TextUtils.muted(" : ") + value;
    }

    private String formatKeyValue(String key, String value) {
        return TextUtils.label(key) + TextUtils.muted(": ") + value;
    }

    private static Map<String, String> createPermissionDescriptions() {
        Map<String, String> permissions = new LinkedHashMap<>();
        permissions.put(PERMISSION_ADMIN, LocaleManager.text("ui.permission-descriptions.admin", "allows all {plugin_name} commands and overrides"));
        permissions.put(PERMISSION_STATUS, LocaleManager.text("ui.permission-descriptions.status", "shows /xmastree status output"));
        permissions.put(PERMISSION_HELP, LocaleManager.text("ui.permission-descriptions.help", "shows /xmastree help output"));
        permissions.put(PERMISSION_INFO, LocaleManager.text("ui.permission-descriptions.info", "shows /xmastree info output"));
        permissions.put(PERMISSION_GIVE, LocaleManager.text("ui.permission-descriptions.give", "allows /xmastree give"));
        permissions.put(PERMISSION_GIFTS, LocaleManager.text("ui.permission-descriptions.gifts", "allows /xmastree gifts"));
        permissions.put(PERMISSION_ADDHAND, LocaleManager.text("ui.permission-descriptions.addhand", "allows /xmastree gifts addhand and /xmastree addhand"));
        permissions.put(PERMISSION_RELOAD, LocaleManager.text("ui.permission-descriptions.reload", "allows /xmastree reload"));
        permissions.put(PERMISSION_INSPECT, LocaleManager.text("ui.permission-descriptions.inspect", "allows /xmastree inspect"));
        permissions.put(PERMISSION_TEST, LocaleManager.text("ui.permission-descriptions.test", "allows /xmastree test"));
        permissions.put(PERMISSION_DATA, LocaleManager.text("ui.permission-descriptions.data", "allows /xmastree data"));
        permissions.put(PERMISSION_JOURNAL, LocaleManager.text("ui.permission-descriptions.journal", "allows /xmastree journal"));
        permissions.put(PERMISSION_MILESTONES, LocaleManager.text("ui.permission-descriptions.milestones", "allows /xmastree milestones"));
        permissions.put(PERMISSION_MILESTONES_CLAIM, LocaleManager.text("ui.permission-descriptions.milestones-claim", "allows /xmastree milestones claim"));
        permissions.put(PERMISSION_COMMUNITY, LocaleManager.text("ui.permission-descriptions.community", "allows /xmastree community"));
        permissions.put(PERMISSION_COMMUNITY_CLAIM, LocaleManager.text("ui.permission-descriptions.community-claim", "allows /xmastree community claim"));
        permissions.put(PERMISSION_DEBUG, LocaleManager.text("ui.permission-descriptions.debug", "allows /xmastree debug"));
        permissions.put(PERMISSION_DEBUG_TOGGLE, LocaleManager.text("ui.permission-descriptions.debug-toggle", "allows /xmastree debug toggle"));
        permissions.put(PERMISSION_END, LocaleManager.text("ui.permission-descriptions.end", "allows /xmastree end"));
        permissions.put(PERMISSION_TREE_OVERRIDE, LocaleManager.text("ui.permission-descriptions.tree-override", "allows managing other players' trees"));
        return permissions;
    }

    private String formatLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return LocaleManager.text("ui.inspect.values.unknown-location", "unknown");
        }
        return location.getWorld().getName()
                + " "
                + location.getBlockX()
                + ", "
                + location.getBlockY()
                + ", "
                + location.getBlockZ();
    }

    private String formatRequirements(Map<Material, Integer> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            if (entry.getValue() > 0) {
                parts.add(formatMaterial(entry.getKey()) + " x" + entry.getValue());
            }
        }
        return parts.isEmpty() ? LocaleManager.text("ui.inspect.values.none", "none") : String.join(", ", parts);
    }

    private String formatItems(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        Map<Material, Integer> totals = new LinkedHashMap<>();
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                totals.merge(item.getType(), item.getAmount(), Integer::sum);
            }
        }
        return formatRequirements(totals);
    }

    private String formatGiftItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return TextUtils.muted(LocaleManager.text("ui.inspect.values.none", "none"));
        }
        String name = formatMaterial(item.getType());
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            name = PLAIN_TEXT.serialize(meta.displayName());
        }
        String amount = " x" + item.getAmount();
        if (name.equals(formatMaterial(item.getType()))) {
            return TextUtils.text(name + amount);
        }
        return TextUtils.text(name + amount) + TextUtils.muted(" (" + item.getType().name() + ")");
    }

    private String formatGiftEntry(Main.GiftEntry gift, int totalWeight) {
        if (gift == null) {
            return TextUtils.muted(LocaleManager.text("ui.inspect.values.none", "none"));
        }
        return formatGiftItem(gift.item())
                + TextUtils.muted(" | ")
                + TextUtils.label(LocaleManager.text("ui.gifts.labels.weight", "Weight"))
                + TextUtils.muted(": ")
                + TextUtils.text(Integer.toString(gift.weight()))
                + TextUtils.muted(" | ")
                + TextUtils.label(LocaleManager.text("ui.gifts.labels.chance", "Chance"))
                + TextUtils.muted(": ")
                + TextUtils.text(formatGiftChance(gift.weight(), totalWeight));
    }

    private String formatGiftChance(int weight, int totalWeight) {
        if (totalWeight <= 0) {
            return "0%";
        }
        double chance = (weight * 100.0) / totalWeight;
        return String.format(Locale.ENGLISH, "%.2f%%", chance);
    }

    private List<String> giftIndexes() {
        List<String> indexes = new ArrayList<>();
        int count = plugin.getGiftEntries().size();
        for (int index = 1; index <= count; index++) {
            indexes.add(Integer.toString(index));
        }
        return indexes;
    }

    private List<String> playerMilestoneKeys() {
        List<String> keys = new ArrayList<>();
        if (plugin.getEventProgress() != null) {
            for (EventProgress.Milestone milestone : plugin.getEventProgress().playerMilestones()) {
                keys.add(milestone.key());
            }
        }
        return keys;
    }

    private List<String> communityMilestoneKeys() {
        List<String> keys = new ArrayList<>();
        if (plugin.getEventProgress() != null) {
            for (EventProgress.Milestone milestone : plugin.getEventProgress().communityMilestones()) {
                keys.add(milestone.key());
            }
        }
        return keys;
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        addOnlinePlayerNames(names);
        return names;
    }

    private void addOnlinePlayerNames(List<String> names) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
    }

    private OfflinePlayer resolvePlayerTarget(CommandSender sender, String targetName) {
        if (targetName == null || targetName.isBlank()) {
            if (sender instanceof Player player) {
                return player;
            }
            return null;
        }
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayerIfCached(targetName);
    }

    private boolean canViewTarget(CommandSender sender, OfflinePlayer target) {
        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            return true;
        }
        return canOverrideTree(sender);
    }

    private int countOwnerTrees(UUID ownerId) {
        int count = 0;
        for (MagicTree tree : XMas.getAllTrees()) {
            if (tree.getOwner().equals(ownerId)) {
                count++;
            }
        }
        return count;
    }

    private String highestTreeLevelName(UUID ownerId) {
        TreeLevel highestLevel = null;
        int highestRank = 0;
        for (MagicTree tree : XMas.getAllTrees()) {
            if (!tree.getOwner().equals(ownerId)) {
                continue;
            }
            int rank = treeLevelRank(tree.getLevel());
            if (rank > highestRank) {
                highestRank = rank;
                highestLevel = tree.getLevel();
            }
        }
        return highestLevel == null
                ? LocaleManager.text("ui.inspect.values.none", "none")
                : highestLevel.getLevelName();
    }

    private int treeLevelRank(TreeLevel level) {
        if (level == null || level.getLevelName() == null) {
            return 0;
        }
        return switch (level.getLevelName().toLowerCase(Locale.ENGLISH)) {
            case "sapling" -> 1;
            case "small_tree" -> 2;
            case "tree" -> 3;
            case "magic_tree" -> 4;
            case "super_tree" -> 5;
            default -> 0;
        };
    }

    private String formatNextLevel(MagicTree tree) {
        TreeLevel nextLevel = tree.getLevel().getNextLevel();
        if (nextLevel == null) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        return nextLevel.getLevelName();
    }

    private boolean isChunkLoaded(Location location) {
        return location != null
                && location.getWorld() != null
                && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private boolean canNextLevelFit(MagicTree tree) {
        TreeLevel nextLevel = tree.getLevel().getNextLevel();
        Location location = tree.getLocation();
        return nextLevel != null
                && location != null
                && location.getWorld() != null
                && nextLevel.getStructureTemplate().canGrow(location);
    }

    private String formatTicks(long updateCycles) {
        if (updateCycles < 0) {
            return LocaleManager.text("ui.inspect.values.disabled", "disabled");
        }
        long seconds = Math.max(0, updateCycles * Main.getUpdateSpeed() / 20);
        if (seconds < 60) {
            return updateCycles + " cycles (" + seconds + "s)";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return updateCycles + " cycles (" + minutes + "m " + remainingSeconds + "s)";
    }

    private String formatRequirementProgress(MagicTree tree) {
        Map<Material, Integer> totalRequirements = tree.getLevel().getLevelupRequirements();
        if (totalRequirements == null || totalRequirements.isEmpty()) {
            return LocaleManager.text("ui.inspect.values.none", "none");
        }
        int total = 0;
        int spent = 0;
        for (Map.Entry<Material, Integer> entry : totalRequirements.entrySet()) {
            int required = Math.max(0, entry.getValue());
            int remaining = Math.max(0, tree.getLevelupRequirements().getOrDefault(entry.getKey(), 0));
            total += required;
            spent += Math.max(0, required - remaining);
        }
        return spent + " / " + total;
    }

    private String formatSpentResources(MagicTree tree) {
        Map<Material, Integer> spentResources = new LinkedHashMap<>();
        Map<Material, Integer> totalRequirements = tree.getLevel().getLevelupRequirements();
        if (totalRequirements != null) {
            for (Map.Entry<Material, Integer> entry : totalRequirements.entrySet()) {
                int required = Math.max(0, entry.getValue());
                int remaining = Math.max(0, tree.getLevelupRequirements().getOrDefault(entry.getKey(), 0));
                int spent = Math.max(0, required - remaining);
                if (spent > 0) {
                    spentResources.put(entry.getKey(), spent);
                }
            }
        }
        return formatRequirements(spentResources);
    }

    private List<String> getWorldNames() {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return worlds;
    }

    private String formatMaterial(Material material) {
        return Arrays.stream(material.name().toLowerCase(Locale.ENGLISH).split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(material.name());
    }

    private String formatPlain(String label, Object value) {
        return label + ": " + value;
    }

    private static Map<String, String> createDebugSections() {
        Map<String, String> sections = new LinkedHashMap<>();
        sections.put("status", "Status");
        sections.put("commands", "Commands");
        sections.put("permissions", "Permissions");
        sections.put("placeholders", "Placeholders");
        sections.put("config", "Config");
        sections.put("diagnostics", "Diagnostics");
        return sections;
    }

    private String warningCount(int value) {
        return value > 0 ? TextUtils.warning(Integer.toString(value)) : TextUtils.success("0");
    }

    private String debugSectionDisplayName(String sectionKey) {
        return LocaleManager.text("ui.debug.sections." + sectionKey, DEBUG_SECTIONS.getOrDefault(sectionKey, sectionKey));
    }

    private String replaceToken(String template, String key, String value) {
        if (template == null) {
            return null;
        }
        return template.replace("{" + key + "}", TextUtils.escape(value));
    }

}
