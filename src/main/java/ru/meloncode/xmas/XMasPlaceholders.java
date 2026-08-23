package ru.meloncode.xmas;

import org.bukkit.OfflinePlayer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class XMasPlaceholders {
    public static final String IDENTIFIER = "onembxmastree";
    public static final List<String> EXAMPLES = Arrays.asList(
            "%onembxmastree_event.active%",
            "%onembxmastree_event.active_text%",
            "%onembxmastree_event.status%",
            "%onembxmastree_event.starts_at%",
            "%onembxmastree_event.ends_at%",
            "%onembxmastree_event.ends_in%",
            "%onembxmastree_event.ends_timestamp%",
            "%onembxmastree_event.auto_end%",
            "%onembxmastree_resource.back%",
            "%onembxmastree_resource.back_text%",
            "%onembxmastree_particles.enabled%",
            "%onembxmastree_luck.enabled%",
            "%onembxmastree_luck.chance%",
            "%onembxmastree_trees.total%",
            "%onembxmastree_trees.owners%",
            "%onembxmastree_player.trees%",
            "%onembxmastree_player.highest_tree_level%",
            "%onembxmastree_player.highest_tree_level_name%",
            "%onembxmastree_player.highest_tree_level_display%",
            "%onembxmastree_player.gifts_today%",
            "%onembxmastree_player.gifts_total%",
            "%onembxmastree_player.streak_current%",
            "%onembxmastree_player.streak_best%",
            "%onembxmastree_player.milestones_claimable%",
            "%onembxmastree_community.gifts_opened%",
            "%onembxmastree_version%"
    );
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");

    private XMasPlaceholders() {
    }

    public static String resolve(Main plugin, OfflinePlayer player, String params) {
        if (params == null || params.trim().isEmpty()) {
            return null;
        }
        String key = normalize(params);
        return switch (key) {
            case "event_active" -> Boolean.toString(Main.inProgress);
            case "event_active_text" -> Main.inProgress
                    ? LocaleManager.text("placeholders.values.active", "Active")
                    : LocaleManager.text("placeholders.values.inactive", "Inactive");
            case "event_status" -> Main.inProgress
                    ? LocaleManager.text("placeholders.values.in-progress", "In Progress")
                    : LocaleManager.text("placeholders.values.holidays-end", "Holidays End");
            case "event_starts_at" -> LocaleManager.text("placeholders.values.manual", "manual");
            case "event_ends_at" -> formatEndDate();
            case "event_ends_in" -> formatDurationUntilEnd();
            case "event_ends_timestamp" -> Long.toString(Main.endTime);
            case "event_auto_end" -> Boolean.toString(Main.autoEnd);
            case "resource_back" -> Boolean.toString(Main.resourceBack);
            case "resource_back_text" -> Main.resourceBack
                    ? LocaleManager.text("placeholders.values.yes", "Yes")
                    : LocaleManager.text("placeholders.values.no", "No");
            case "particles_enabled" -> Boolean.toString(Main.particlesEnabled);
            case "luck_enabled" -> Boolean.toString(Main.LUCK_CHANCE_ENABLED);
            case "luck_chance" -> Integer.toString(Math.round(Main.LUCK_CHANCE * 100));
            case "trees_total" -> Integer.toString(XMas.getAllTrees().size());
            case "trees_owners" -> Integer.toString(countOwners(XMas.getAllTrees()));
            case "player_trees" -> Integer.toString(countPlayerTrees(player));
            case "player_highest_tree_level" -> Integer.toString(highestPlayerTreeRank(player));
            case "player_highest_tree_level_name" -> highestPlayerTreeLevelName(player);
            case "player_highest_tree_level_display" -> displayTreeLevel(highestPlayerTreeLevelName(player));
            case "player_gifts_today" -> Integer.toString(player != null && plugin.getEventProgress() != null ? plugin.getEventProgress().getGiftClaimsToday(player.getUniqueId()) : 0);
            case "player_gifts_total" -> Integer.toString(player != null && plugin.getEventProgress() != null ? plugin.getEventProgress().getTotalGiftClaims(player.getUniqueId()) : 0);
            case "player_streak_current" -> Integer.toString(player != null && plugin.getEventProgress() != null ? plugin.getEventProgress().getCurrentStreak(player.getUniqueId()) : 0);
            case "player_streak_best" -> Integer.toString(player != null && plugin.getEventProgress() != null ? plugin.getEventProgress().getBestStreak(player.getUniqueId()) : 0);
            case "player_milestones_claimable" -> Integer.toString(player != null && plugin.getEventProgress() != null ? plugin.getEventProgress().playerStats(player).claimableMilestones() : 0);
            case "community_gifts_opened" -> Integer.toString(plugin.getEventProgress() != null ? plugin.getEventProgress().getCommunityGiftClaims() : 0);
            case "version" -> plugin.getPluginMeta().getVersion();
            default -> null;
        };
    }

    private static String normalize(String params) {
        return params.trim()
                .toLowerCase(Locale.ENGLISH)
                .replace('.', '_')
                .replace('-', '_');
    }

    private static String formatEndDate() {
        if (Main.endTime <= 0) {
            return LocaleManager.text("placeholders.values.unknown", "unknown");
        }
        return DATE_FORMAT.format(new Date(Main.endTime));
    }

    private static String formatDurationUntilEnd() {
        if (!Main.autoEnd) {
            return LocaleManager.text("placeholders.values.disabled", "disabled");
        }
        if (Main.endTime <= 0) {
            return LocaleManager.text("placeholders.values.unknown", "unknown");
        }
        long remainingMillis = Main.endTime - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return LocaleManager.text("placeholders.values.ended", "ended");
        }

        long totalSeconds = remainingMillis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (days > 0) {
            return LocaleManager.text("placeholders.values.duration-days-hours", "{days}d {hours}h",
                    "{days}", Long.toString(days),
                    "{hours}", Long.toString(hours));
        }
        if (hours > 0) {
            return LocaleManager.text("placeholders.values.duration-hours-minutes", "{hours}h {minutes}m",
                    "{hours}", Long.toString(hours),
                    "{minutes}", Long.toString(minutes));
        }
        return LocaleManager.text("placeholders.values.duration-minutes", "{minutes}m",
                "{minutes}", Long.toString(minutes));
    }

    private static int countOwners(Collection<MagicTree> trees) {
        Set<UUID> owners = new LinkedHashSet<>();
        for (MagicTree tree : trees) {
            owners.add(tree.getOwner());
        }
        return owners.size();
    }

    private static int countPlayerTrees(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return 0;
        }
        int count = 0;
        for (MagicTree tree : XMas.getAllTrees()) {
            if (player.getUniqueId().equals(tree.getOwner())) {
                count++;
            }
        }
        return count;
    }

    private static int highestPlayerTreeRank(OfflinePlayer player) {
        TreeLevel highestLevel = highestPlayerTreeLevel(player);
        return highestLevel == null ? 0 : treeLevelRank(highestLevel);
    }

    private static String highestPlayerTreeLevelName(OfflinePlayer player) {
        TreeLevel highestLevel = highestPlayerTreeLevel(player);
        return highestLevel == null
                ? LocaleManager.text("placeholders.values.none", "none")
                : highestLevel.getLevelName();
    }

    private static TreeLevel highestPlayerTreeLevel(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return null;
        }
        TreeLevel highestLevel = null;
        int highestRank = 0;
        for (MagicTree tree : XMas.getAllTrees()) {
            if (!player.getUniqueId().equals(tree.getOwner())) {
                continue;
            }
            int rank = treeLevelRank(tree.getLevel());
            if (rank > highestRank) {
                highestRank = rank;
                highestLevel = tree.getLevel();
            }
        }
        return highestLevel;
    }

    private static int treeLevelRank(TreeLevel level) {
        if (level == null) {
            return 0;
        }
        String levelName = level.getLevelName();
        if (levelName == null) {
            return 0;
        }
        return switch (levelName.toLowerCase(Locale.ENGLISH)) {
            case "sapling" -> 1;
            case "small_tree" -> 2;
            case "tree" -> 3;
            case "magic_tree" -> 4;
            case "super_tree" -> 5;
            default -> 0;
        };
    }

    private static String displayTreeLevel(String levelName) {
        if (levelName == null || levelName.isBlank() || levelName.equalsIgnoreCase(LocaleManager.text("placeholders.values.none", "none"))) {
            return LocaleManager.text("placeholders.values.none", "none");
        }
        return switch (levelName.toLowerCase(Locale.ENGLISH)) {
            case "sapling" -> LocaleManager.text("placeholders.values.tree-level.sapling", "Sapling");
            case "small_tree" -> LocaleManager.text("placeholders.values.tree-level.small-tree", "Small Tree");
            case "tree" -> LocaleManager.text("placeholders.values.tree-level.tree", "Tree");
            case "magic_tree" -> LocaleManager.text("placeholders.values.tree-level.magic-tree", "Magic Tree");
            case "super_tree" -> LocaleManager.text("placeholders.values.tree-level.super-tree", "Super Tree");
            default -> levelName;
        };
    }

    public static Map<String, String> descriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        descriptions.put("%onembxmastree_event.active%", LocaleManager.text("placeholders.descriptions.event-active", "whether the event is currently active"));
        descriptions.put("%onembxmastree_event.active_text%", LocaleManager.text("placeholders.descriptions.event-active-text", "human-readable active state"));
        descriptions.put("%onembxmastree_event.status%", LocaleManager.text("placeholders.descriptions.event-status", "current event status text"));
        descriptions.put("%onembxmastree_event.starts_at%", LocaleManager.text("placeholders.descriptions.event-starts-at", "event start mode"));
        descriptions.put("%onembxmastree_event.ends_at%", LocaleManager.text("placeholders.descriptions.event-ends-at", "configured event end date"));
        descriptions.put("%onembxmastree_event.ends_in%", LocaleManager.text("placeholders.descriptions.event-ends-in", "time remaining until the event ends"));
        descriptions.put("%onembxmastree_event.ends_timestamp%", LocaleManager.text("placeholders.descriptions.event-ends-timestamp", "event end timestamp in milliseconds"));
        descriptions.put("%onembxmastree_event.auto_end%", LocaleManager.text("placeholders.descriptions.event-auto-end", "whether automatic ending is enabled"));
        descriptions.put("%onembxmastree_resource.back%", LocaleManager.text("placeholders.descriptions.resource-back", "whether resource refunds are enabled"));
        descriptions.put("%onembxmastree_resource.back_text%", LocaleManager.text("placeholders.descriptions.resource-back-text", "human-readable refund state"));
        descriptions.put("%onembxmastree_particles.enabled%", LocaleManager.text("placeholders.descriptions.particles-enabled", "whether XMas Tree particles are enabled"));
        descriptions.put("%onembxmastree_luck.enabled%", LocaleManager.text("placeholders.descriptions.luck-enabled", "whether gift luck chance is enabled"));
        descriptions.put("%onembxmastree_luck.chance%", LocaleManager.text("placeholders.descriptions.luck-chance", "gift luck chance as a percent"));
        descriptions.put("%onembxmastree_trees.total%", LocaleManager.text("placeholders.descriptions.trees-total", "total loaded tree count"));
        descriptions.put("%onembxmastree_trees.owners%", LocaleManager.text("placeholders.descriptions.trees-owners", "number of unique tree owners"));
        descriptions.put("%onembxmastree_player.trees%", LocaleManager.text("placeholders.descriptions.player-trees", "loaded trees owned by the placeholder player"));
        descriptions.put("%onembxmastree_player.highest_tree_level%", LocaleManager.text("placeholders.descriptions.player-highest-tree-level", "highest loaded tree level owned by the placeholder player, as 0-5"));
        descriptions.put("%onembxmastree_player.highest_tree_level_name%", LocaleManager.text("placeholders.descriptions.player-highest-tree-level-name", "highest loaded tree level owned by the placeholder player, as a config key"));
        descriptions.put("%onembxmastree_player.highest_tree_level_display%", LocaleManager.text("placeholders.descriptions.player-highest-tree-level-display", "highest loaded tree level owned by the placeholder player, as display text"));
        descriptions.put("%onembxmastree_player.gifts_today%", LocaleManager.text("placeholders.descriptions.player-gifts-today", "present gifts opened by the placeholder player today"));
        descriptions.put("%onembxmastree_player.gifts_total%", LocaleManager.text("placeholders.descriptions.player-gifts-total", "present gifts opened by the placeholder player during the event"));
        descriptions.put("%onembxmastree_player.streak_current%", LocaleManager.text("placeholders.descriptions.player-streak-current", "current daily present-opening streak for the placeholder player"));
        descriptions.put("%onembxmastree_player.streak_best%", LocaleManager.text("placeholders.descriptions.player-streak-best", "best daily present-opening streak for the placeholder player"));
        descriptions.put("%onembxmastree_player.milestones_claimable%", LocaleManager.text("placeholders.descriptions.player-milestones-claimable", "claimable personal milestone count for the placeholder player"));
        descriptions.put("%onembxmastree_community.gifts_opened%", LocaleManager.text("placeholders.descriptions.community-gifts-opened", "community-wide present gifts opened"));
        descriptions.put("%onembxmastree_version%", LocaleManager.text("placeholders.descriptions.version", "loaded plugin version"));
        return descriptions;
    }
}
