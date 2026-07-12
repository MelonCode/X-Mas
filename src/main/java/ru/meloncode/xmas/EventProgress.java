package ru.meloncode.xmas;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.meloncode.xmas.utils.ConfigUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EventProgress {
    private static final String PROGRESS_FILE = "progress.yml";
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HISTORY_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int HISTORY_LIMIT = 25;

    private final Main plugin;
    private final File file;
    private YamlConfiguration data;
    private final Map<String, Milestone> playerMilestones = new LinkedHashMap<>();
    private final Map<String, Milestone> communityMilestones = new LinkedHashMap<>();
    private BukkitTask saveTask;
    private boolean dirty;

    public EventProgress(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), PROGRESS_FILE);
        reload();
    }

    public void reload() {
        flushSave();
        data = ConfigUtils.loadConfig(file);
        reloadMilestones();
    }

    public void save() {
        flushSave();
    }

    public void flushSave() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        if (data == null) {
            return;
        }
        if (!dirty && file.exists()) {
            return;
        }
        ConfigUtils.saveConfig(file, data);
        dirty = false;
    }

    public void recordGiftClaim(Player player) {
        if (player == null) {
            return;
        }
        String playerPath = playerPath(player.getUniqueId());
        String today = today();
        data.set(playerPath + ".name", player.getName());
        data.set(playerPath + ".total-gifts", data.getInt(playerPath + ".total-gifts", 0) + 1);
        data.set(playerPath + ".daily." + today, data.getInt(playerPath + ".daily." + today, 0) + 1);
        data.set(playerPath + ".current-streak", calculateCurrentStreak(player.getUniqueId()));
        data.set(playerPath + ".best-streak", Math.max(getBestStreak(player.getUniqueId()), getCurrentStreak(player.getUniqueId())));
        data.set("community.gifts-opened", getCommunityGiftClaims() + 1);
        markDirty();
    }

    public void recordTreeCreated(Player player, MagicTree tree) {
        if (player == null || tree == null) {
            return;
        }
        data.set(playerPath(player.getUniqueId()) + ".name", player.getName());
        addPlayerHistory(player.getUniqueId(), "tree-history", LocaleManager.text("progress.history.tree-created", "{time} - Created {level} at {location} ({tree})",
                "{time}", historyTimestamp(),
                "{level}", tree.getLevel().getLevelName(),
                "{location}", formatLocation(tree),
                "{tree}", tree.getTreeUID().toString()));
    }

    public void recordTreeRemoved(MagicTree tree) {
        if (tree == null) {
            return;
        }
        addPlayerHistory(tree.getOwner(), "tree-history", LocaleManager.text("progress.history.tree-removed", "{time} - Removed {level} at {location} ({tree})",
                "{time}", historyTimestamp(),
                "{level}", tree.getLevel().getLevelName(),
                "{location}", formatLocation(tree),
                "{tree}", tree.getTreeUID().toString()));
    }

    public PlayerStats playerStats(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return new PlayerStats(
                uuid,
                player.getName() != null ? player.getName() : data.getString(playerPath(uuid) + ".name", uuid.toString()),
                getTotalGiftClaims(uuid),
                getGiftClaimsToday(uuid),
                getCurrentStreak(uuid),
                getBestStreak(uuid),
                countPlayerClaimableMilestones(uuid)
        );
    }

    public int getTotalGiftClaims(UUID uuid) {
        return data.getInt(playerPath(uuid) + ".total-gifts", 0);
    }

    public int getGiftClaimsToday(UUID uuid) {
        return data.getInt(playerPath(uuid) + ".daily." + today(), 0);
    }

    public int getCurrentStreak(UUID uuid) {
        return data.getInt(playerPath(uuid) + ".current-streak", calculateCurrentStreak(uuid));
    }

    public int getBestStreak(UUID uuid) {
        return data.getInt(playerPath(uuid) + ".best-streak", 0);
    }

    public int getCommunityGiftClaims() {
        return data.getInt("community.gifts-opened", 0);
    }

    public List<Milestone> playerMilestones() {
        return List.copyOf(playerMilestones.values());
    }

    public List<Milestone> communityMilestones() {
        return List.copyOf(communityMilestones.values());
    }

    public ClaimResult claimPlayerMilestone(Player player, String key) {
        Milestone milestone = playerMilestones.get(normalizeKey(key));
        if (milestone == null) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.unknown", "Unknown milestone."));
        }
        UUID uuid = player.getUniqueId();
        if (isPlayerMilestoneClaimed(uuid, milestone.key())) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.already-claimed", "That milestone is already claimed."));
        }
        if (!isPlayerMilestoneEligible(uuid, milestone)) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.not-ready", "That milestone is not ready yet."));
        }
        int progress = progressValue(uuid, milestone);
        data.set(playerPath(uuid) + ".claimed." + milestone.key(), System.currentTimeMillis());
        runRewardCommands(milestone, player.getName(), player.getName(), progress);
        addPlayerHistory(uuid, "milestone-history", LocaleManager.text("progress.history.player-milestone-claimed", "{time} - Claimed {display_name} ({key}) at {progress}/{target}",
                "{time}", historyTimestamp(),
                "{display_name}", milestone.displayName(),
                "{key}", milestone.key(),
                "{progress}", Integer.toString(progress),
                "{target}", Integer.toString(milestone.target())));
        markDirty();
        return ClaimResult.success(LocaleManager.text("progress.milestone.claimed", "Milestone claimed!"));
    }

    public ClaimResult claimCommunityMilestone(CommandSender sender, String key) {
        Milestone milestone = communityMilestones.get(normalizeKey(key));
        if (milestone == null) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.unknown", "Unknown milestone."));
        }
        if (isCommunityMilestoneClaimed(milestone.key())) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.already-claimed", "That milestone is already claimed."));
        }
        if (!isCommunityMilestoneEligible(milestone)) {
            return ClaimResult.error(LocaleManager.text("progress.milestone.not-ready", "That milestone is not ready yet."));
        }
        int progress = getCommunityGiftClaims();
        data.set("community.claimed." + milestone.key(), System.currentTimeMillis());
        runRewardCommands(milestone, sender.getName(), sender.getName(), progress);
        addHistory("community.milestone-history", LocaleManager.text("progress.history.community-milestone-claimed", "{time} - {claimer} claimed {display_name} ({key}) at {progress}/{target}",
                "{time}", historyTimestamp(),
                "{claimer}", sender.getName(),
                "{display_name}", milestone.displayName(),
                "{key}", milestone.key(),
                "{progress}", Integer.toString(progress),
                "{target}", Integer.toString(milestone.target())));
        markDirty();
        return ClaimResult.success(LocaleManager.text("progress.milestone.claimed", "Milestone claimed!"));
    }

    public List<String> playerTreeHistory(OfflinePlayer player, int limit) {
        return recentHistory(playerPath(player.getUniqueId()) + ".tree-history", limit);
    }

    public List<String> playerMilestoneHistory(OfflinePlayer player, int limit) {
        return recentHistory(playerPath(player.getUniqueId()) + ".milestone-history", limit);
    }

    public List<String> communityMilestoneHistory(int limit) {
        return recentHistory("community.milestone-history", limit);
    }

    public EventSummary eventSummary() {
        int playersWithGifts = 0;
        int claimedPlayerMilestones = 0;
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players != null) {
            for (String playerKey : players.getKeys(false)) {
                String path = "players." + playerKey;
                if (data.getInt(path + ".total-gifts", 0) > 0) {
                    playersWithGifts++;
                }
                ConfigurationSection claimed = data.getConfigurationSection(path + ".claimed");
                if (claimed != null) {
                    claimedPlayerMilestones += claimed.getKeys(false).size();
                }
            }
        }
        ConfigurationSection claimedCommunity = data.getConfigurationSection("community.claimed");
        return new EventSummary(
                getCommunityGiftClaims(),
                playersWithGifts,
                claimedPlayerMilestones,
                claimedCommunity != null ? claimedCommunity.getKeys(false).size() : 0
        );
    }

    public RewardPreview previewPlayerMilestone(OfflinePlayer player, String key, boolean requireReady) {
        Milestone milestone = playerMilestones.get(normalizeKey(key));
        if (milestone == null) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.unknown", "Unknown milestone."));
        }
        UUID uuid = player.getUniqueId();
        if (isPlayerMilestoneClaimed(uuid, milestone.key())) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.already-claimed", "That milestone is already claimed."));
        }
        if (requireReady && !isPlayerMilestoneEligible(uuid, milestone)) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.not-ready", "That milestone is not ready yet."));
        }
        int progress = progressValue(uuid, milestone);
        String playerName = player.getName() != null ? player.getName() : data.getString(playerPath(uuid) + ".name", uuid.toString());
        return RewardPreview.success(milestone, progress, renderRewardCommands(milestone, playerName, playerName, progress));
    }

    public RewardPreview previewCommunityMilestone(CommandSender sender, String key, boolean requireReady) {
        Milestone milestone = communityMilestones.get(normalizeKey(key));
        if (milestone == null) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.unknown", "Unknown milestone."));
        }
        if (isCommunityMilestoneClaimed(milestone.key())) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.already-claimed", "That milestone is already claimed."));
        }
        if (requireReady && !isCommunityMilestoneEligible(milestone)) {
            return RewardPreview.error(LocaleManager.text("progress.milestone.not-ready", "That milestone is not ready yet."));
        }
        int progress = getCommunityGiftClaims();
        return RewardPreview.success(milestone, progress, renderRewardCommands(milestone, sender.getName(), sender.getName(), progress));
    }

    public boolean isPlayerMilestoneEligible(UUID uuid, Milestone milestone) {
        int value = switch (milestone.type()) {
            case DAILY_GIFTS -> getGiftClaimsToday(uuid);
            case TOTAL_GIFTS -> getTotalGiftClaims(uuid);
            case STREAK_DAYS -> getCurrentStreak(uuid);
            case COMMUNITY_GIFTS -> getCommunityGiftClaims();
        };
        return value >= milestone.target();
    }

    public boolean isCommunityMilestoneEligible(Milestone milestone) {
        return switch (milestone.type()) {
            case COMMUNITY_GIFTS -> getCommunityGiftClaims() >= milestone.target();
            default -> false;
        };
    }

    public boolean isPlayerMilestoneClaimed(UUID uuid, String key) {
        return data.contains(playerPath(uuid) + ".claimed." + normalizeKey(key), true);
    }

    public boolean isCommunityMilestoneClaimed(String key) {
        return data.contains("community.claimed." + normalizeKey(key), true);
    }

    public int progressValue(UUID uuid, Milestone milestone) {
        return switch (milestone.type()) {
            case DAILY_GIFTS -> getGiftClaimsToday(uuid);
            case TOTAL_GIFTS -> getTotalGiftClaims(uuid);
            case STREAK_DAYS -> getCurrentStreak(uuid);
            case COMMUNITY_GIFTS -> getCommunityGiftClaims();
        };
    }

    private int countPlayerClaimableMilestones(UUID uuid) {
        int count = 0;
        for (Milestone milestone : playerMilestones.values()) {
            if (!isPlayerMilestoneClaimed(uuid, milestone.key()) && isPlayerMilestoneEligible(uuid, milestone)) {
                count++;
            }
        }
        return count;
    }

    private int calculateCurrentStreak(UUID uuid) {
        int streak = 0;
        LocalDate day = LocalDate.now(ZoneId.systemDefault());
        String dailyPath = playerPath(uuid) + ".daily.";
        while (data.getInt(dailyPath + day, 0) > 0) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private void reloadMilestones() {
        playerMilestones.clear();
        communityMilestones.clear();
        loadMilestones("xmas.milestones.player", playerMilestones);
        loadMilestones("xmas.milestones.community", communityMilestones);
    }

    private void loadMilestones(String path, Map<String, Milestone> target) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection milestoneSection = section.getConfigurationSection(key);
            if (milestoneSection == null || !milestoneSection.getBoolean("enabled", true)) {
                continue;
            }
            MilestoneType type = MilestoneType.fromConfig(milestoneSection.getString("type", ""));
            int targetValue = milestoneSection.getInt("target", 1);
            if (type == null || targetValue < 1) {
                plugin.getLogger().warning(LocaleManager.text("progress.milestone.invalid", "Skipping invalid milestone {key}",
                        "{key}", path + "." + key));
                continue;
            }
            String normalizedKey = normalizeKey(key);
            target.put(normalizedKey, new Milestone(
                    normalizedKey,
                    milestoneSection.getString("display-name", key),
                    type,
                    targetValue,
                    milestoneSection.getStringList("reward-commands")
            ));
        }
    }

    public File backupProgressFile() throws IOException {
        flushSave();
        File backupDirectory = new File(plugin.getDataFolder(), "backups");
        if (!backupDirectory.exists() && !backupDirectory.mkdirs()) {
            throw new IOException("Unable to create backup directory " + backupDirectory.getPath());
        }
        if (!file.exists()) {
            ConfigUtils.saveConfig(file, data);
        }
        File backupFile = new File(backupDirectory, "progress-" + LocalDateTime.now().format(BACKUP_TIMESTAMP) + ".yml");
        Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        return backupFile;
    }

    private void markDirty() {
        dirty = true;
        if (saveTask != null) {
            return;
        }
        saveTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            saveTask = null;
            flushSave();
        }, 100L);
    }

    private void addPlayerHistory(UUID uuid, String historyKey, String line) {
        addHistory(playerPath(uuid) + "." + historyKey, line);
    }

    private void addHistory(String path, String line) {
        List<String> history = data.getStringList(path);
        history.add(line);
        while (history.size() > HISTORY_LIMIT) {
            history.removeFirst();
        }
        data.set(path, history);
        markDirty();
    }

    private List<String> recentHistory(String path, int limit) {
        List<String> history = new ArrayList<>(data.getStringList(path));
        Collections.reverse(history);
        if (limit > 0 && history.size() > limit) {
            return List.copyOf(history.subList(0, limit));
        }
        return List.copyOf(history);
    }

    private String historyTimestamp() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(HISTORY_TIMESTAMP);
    }

    private String formatLocation(MagicTree tree) {
        if (tree.getLocation() == null || tree.getLocation().getWorld() == null) {
            return "unknown";
        }
        return tree.getLocation().getWorld().getName()
                + " "
                + tree.getLocation().getBlockX()
                + ","
                + tree.getLocation().getBlockY()
                + ","
                + tree.getLocation().getBlockZ();
    }

    private List<String> renderRewardCommands(Milestone milestone, String playerName, String claimerName, int progress) {
        List<String> commands = new ArrayList<>();
        for (String command : milestone.rewardCommands()) {
            if (command == null || command.isBlank()) {
                continue;
            }
            String normalized = command.trim();
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            normalized = normalized
                    .replace("{player}", playerName)
                    .replace("{claimer}", claimerName)
                    .replace("{milestone}", milestone.key())
                    .replace("{display_name}", milestone.displayName())
                    .replace("{target}", Integer.toString(milestone.target()))
                    .replace("{progress}", Integer.toString(progress));
            commands.add(normalized);
        }
        return commands;
    }

    private void runRewardCommands(Milestone milestone, String playerName, String claimerName, int progress) {
        for (String command : renderRewardCommands(milestone, playerName, claimerName, progress)) {
            boolean accepted = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            if (accepted) {
                plugin.getLogger().info("Milestone reward command accepted for " + milestone.key() + ": " + command);
            } else {
                plugin.getLogger().warning("Milestone reward command was not accepted for " + milestone.key() + ": " + command);
            }
        }
    }

    private String today() {
        return LocalDate.now(ZoneId.systemDefault()).toString();
    }

    private String playerPath(UUID uuid) {
        return "players." + uuid;
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.toLowerCase(Locale.ENGLISH).replace(' ', '_');
    }

    public record Milestone(String key, String displayName, MilestoneType type, int target, List<String> rewardCommands) {
        public Milestone {
            rewardCommands = rewardCommands == null ? Collections.emptyList() : List.copyOf(new ArrayList<>(rewardCommands));
        }
    }

    public record RewardPreview(boolean success, String message, Milestone milestone, int progress, List<String> commands) {
        public RewardPreview {
            commands = commands == null ? Collections.emptyList() : List.copyOf(new ArrayList<>(commands));
        }

        public static RewardPreview success(Milestone milestone, int progress, List<String> commands) {
            return new RewardPreview(true, "", milestone, progress, commands);
        }

        public static RewardPreview error(String message) {
            return new RewardPreview(false, message, null, 0, Collections.emptyList());
        }
    }

    public record PlayerStats(UUID uuid, String name, int totalGifts, int giftsToday, int currentStreak, int bestStreak, int claimableMilestones) {
    }

    public record EventSummary(int communityGiftClaims, int playersWithGifts, int claimedPlayerMilestones, int claimedCommunityMilestones) {
    }

    public record ClaimResult(boolean success, String message) {
        public static ClaimResult success(String message) {
            return new ClaimResult(true, message);
        }

        public static ClaimResult error(String message) {
            return new ClaimResult(false, message);
        }
    }

    public enum MilestoneType {
        DAILY_GIFTS,
        TOTAL_GIFTS,
        STREAK_DAYS,
        COMMUNITY_GIFTS;

        static MilestoneType fromConfig(String value) {
            if (value == null) {
                return null;
            }
            try {
                return MilestoneType.valueOf(value.trim().toUpperCase(Locale.ENGLISH).replace('-', '_'));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
}
