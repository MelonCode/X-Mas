package ru.meloncode.xmas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

class TreeSerializer {
    private static final File treesFile = new File(Main.getInstance().getDataFolder() + "/trees.yml");
    private static final FileConfiguration trees = ConfigUtils.loadConfig(treesFile);
    private static final Set<String> loggedWorldAliasMappings = ConcurrentHashMap.newKeySet();
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public record TreeDataValidationReport(
            int storedTreeCount,
            int loadedTreeCount,
            Set<String> invalidTreeIds,
            Set<String> invalidOwners,
            Set<String> invalidLevels,
            Set<String> invalidLocations,
            Set<String> missingWorlds,
            Set<String> invalidRequirements,
            Set<String> duplicateLocations
    ) {
        public boolean hasWarnings() {
            return !invalidTreeIds.isEmpty()
                    || !invalidOwners.isEmpty()
                    || !invalidLevels.isEmpty()
                    || !invalidLocations.isEmpty()
                    || !missingWorlds.isEmpty()
                    || !invalidRequirements.isEmpty()
                    || !duplicateLocations.isEmpty();
        }

        public int warningCount() {
            return invalidTreeIds.size()
                    + invalidOwners.size()
                    + invalidLevels.size()
                    + invalidLocations.size()
                    + missingWorlds.size()
                    + invalidRequirements.size()
                    + duplicateLocations.size();
        }
    }

    public record WorldMigrationReport(
            String sourceWorld,
            String targetWorld,
            int matchedTrees,
            boolean applied,
            File backupFile
    ) {
    }

    public record TreeSummary(
            int storedTreeCount,
            int loadedTreeCount,
            Set<UUID> owners,
            Map<String, Integer> levelCounts
    ) {
    }

    public static void loadTrees(JavaPlugin plugin, World world) {
        try {
            UUID owner;
            UUID treeUID;
            TreeLevel level;
            int x, y, z;
            Location loc;
            long presentCounter;
            int scheduledPresents;
            if (trees.getConfigurationSection("trees") != null && trees.getConfigurationSection("trees").getKeys(false).size() > 0) {

                for (String cKey : trees.getConfigurationSection("trees").getKeys(false)) {
                    String savedWorldName = trees.getString("trees." + cKey + ".loc.world");
                    if (matchesSavedWorld(world, savedWorldName)) {
                        try {
                            treeUID = UUID.fromString(cKey);
                            owner = UUID.fromString(trees.getString("trees." + cKey + ".owner"));
                            level = TreeLevel.fromString(trees.getString("trees." + cKey + ".level"));
                            x = trees.getInt("trees." + cKey + ".loc.x");
                            y = trees.getInt("trees." + cKey + ".loc.y");
                            z = trees.getInt("trees." + cKey + ".loc.z");
                            loc = new Location(world, x, y, z);
                            Map<Material, Integer> requirements;
                            ConfigurationSection requirementsSection = trees.getConfigurationSection("trees." + cKey + ".levelup");
                            boolean readyToLevelUp = trees.getBoolean("trees." + cKey + ".ready_to_levelup", false);
                            boolean backfilledRequirements = false;
                            if (readyToLevelUp) {
                                requirements = new HashMap<>();
                            } else if (requirementsSection != null) {
                                requirements = convertRequirementsMap(requirementsSection.getValues(false));
                                if (shouldBackfillLegacyRequirements(level, requirements)) {
                                    requirements = new HashMap<>(level.getLevelupRequirements());
                                    backfilledRequirements = true;
                                }
                            } else if (level.hasNext()) {
                                requirements = new HashMap<>(level.getLevelupRequirements());
                                backfilledRequirements = shouldBackfillLegacyRequirements(level, null);
                            } else {
                                requirements = new HashMap<>();
                            }
                            presentCounter = trees.getLong("trees." + cKey + ".present_counter", 0);
                            scheduledPresents = trees.getInt("trees." + cKey + ".scheduled_presents", 0);

                            MagicTree tree = new MagicTree(owner, treeUID, level, loc, requirements, presentCounter, scheduledPresents);
                            XMas.addMagicTree(tree);
                            if (backfilledRequirements) {
                                tree.save();
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, LocaleManager.text("console.trees.load-tree-error", "Error while loading tree {tree}",
                                    "{tree}", cKey), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.trees.load-error", "Error while loading trees")));
            plugin.getLogger().log(Level.SEVERE, LocaleManager.text("console.trees.unable-load", "Unable to load X-Mas trees"), e);
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
        trees.set("trees." + cKey + ".levelup", null);
        boolean readyToLevelUp = tree.getLevel().hasNext()
                && (tree.getLevelupRequirements() == null || tree.getLevelupRequirements().isEmpty());
        trees.set("trees." + cKey + ".ready_to_levelup", readyToLevelUp ? true : null);
        if (tree.getLevelupRequirements() != null && tree.getLevelupRequirements().size() > 0)
            trees.createSection("trees." + cKey + ".levelup", tree.getLevelupRequirements());
        trees.set("trees." + cKey + ".present_counter", tree.getPresentCounter());
        trees.set("trees." + cKey + ".scheduled_presents", tree.getScheduledPresents());
        try {
            trees.save(treesFile);
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, LocaleManager.text("console.trees.unable-save", "Unable to save X-Mas tree data"), e);
        }
    }

    public static void removeTree(MagicTree tree) {
        trees.set("trees." + tree.getTreeUID().toString(), null);
        try {
            trees.save(treesFile);
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, LocaleManager.text("console.trees.unable-remove", "Unable to remove X-Mas tree data"), e);
        }
    }

    public static File getTreesFile() {
        return treesFile;
    }

    public static File backupTreesFile() throws IOException {
        File backupDirectory = new File(Main.getInstance().getDataFolder(), "backups");
        if (!backupDirectory.exists() && !backupDirectory.mkdirs()) {
            throw new IOException("Unable to create backup directory " + backupDirectory.getPath());
        }
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        File backupFile = new File(backupDirectory, "trees-" + timestamp + ".yml");
        Files.copy(treesFile.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        return backupFile;
    }

    public static TreeDataValidationReport validateTreesFile() {
        FileConfiguration data = ConfigUtils.loadConfig(treesFile);
        ConfigurationSection treeSection = data.getConfigurationSection("trees");
        Set<String> invalidTreeIds = new LinkedHashSet<>();
        Set<String> invalidOwners = new LinkedHashSet<>();
        Set<String> invalidLevels = new LinkedHashSet<>();
        Set<String> invalidLocations = new LinkedHashSet<>();
        Set<String> missingWorlds = new LinkedHashSet<>();
        Set<String> invalidRequirements = new LinkedHashSet<>();
        Set<String> duplicateLocations = new LinkedHashSet<>();
        Set<String> seenLocations = new HashSet<>();

        if (treeSection == null) {
            return new TreeDataValidationReport(
                    0,
                    XMas.getAllTrees().size(),
                    invalidTreeIds,
                    invalidOwners,
                    invalidLevels,
                    invalidLocations,
                    missingWorlds,
                    invalidRequirements,
                    duplicateLocations
            );
        }

        for (String treeKey : treeSection.getKeys(false)) {
            validateTreeId(treeKey, invalidTreeIds);
            validateUuid(data.getString("trees." + treeKey + ".owner"), treeKey, invalidOwners);
            validateLevel(data.getString("trees." + treeKey + ".level"), treeKey, invalidLevels);
            validateLocation(data, treeKey, missingWorlds, invalidLocations, duplicateLocations, seenLocations);
            validateRequirements(data.getConfigurationSection("trees." + treeKey + ".levelup"), treeKey, invalidRequirements);
        }

        return new TreeDataValidationReport(
                treeSection.getKeys(false).size(),
                XMas.getAllTrees().size(),
                invalidTreeIds,
                invalidOwners,
                invalidLevels,
                invalidLocations,
                missingWorlds,
                invalidRequirements,
                duplicateLocations
        );
    }

    public static TreeSummary summarizeTreesFile() {
        FileConfiguration data = ConfigUtils.loadConfig(treesFile);
        ConfigurationSection treeSection = data.getConfigurationSection("trees");
        Set<UUID> owners = new LinkedHashSet<>();
        Map<String, Integer> levelCounts = new LinkedHashMap<>();

        if (treeSection == null) {
            return new TreeSummary(0, XMas.getAllTrees().size(), owners, levelCounts);
        }

        for (String treeKey : treeSection.getKeys(false)) {
            String rawOwner = data.getString("trees." + treeKey + ".owner");
            if (rawOwner != null) {
                try {
                    owners.add(UUID.fromString(rawOwner));
                } catch (IllegalArgumentException ignored) {
                    // Validation reports invalid owners; summaries skip malformed UUIDs.
                }
            }
            String level = data.getString("trees." + treeKey + ".level", "unknown");
            levelCounts.merge(level, 1, Integer::sum);
        }

        return new TreeSummary(treeSection.getKeys(false).size(), XMas.getAllTrees().size(), owners, levelCounts);
    }

    public static WorldMigrationReport migrateWorldName(String sourceWorld, String targetWorld, boolean apply) throws IOException {
        FileConfiguration data = ConfigUtils.loadConfig(treesFile);
        ConfigurationSection treeSection = data.getConfigurationSection("trees");
        int matchedTrees = 0;

        if (treeSection != null) {
            for (String treeKey : treeSection.getKeys(false)) {
                String path = "trees." + treeKey + ".loc.world";
                String savedWorldName = data.getString(path);
                if (savedWorldName != null && savedWorldName.equalsIgnoreCase(sourceWorld)) {
                    matchedTrees++;
                    if (apply) {
                        data.set(path, targetWorld);
                    }
                }
            }
        }

        File backupFile = null;
        if (apply && matchedTrees > 0) {
            backupFile = backupTreesFile();
            ConfigUtils.saveConfig(treesFile, data);
        }
        return new WorldMigrationReport(sourceWorld, targetWorld, matchedTrees, apply, backupFile);
    }

    public static Map<Material, Integer> convertRequirementsMap(Map<String, Object> map) {
        Map<Material, Integer> levelupRequirements = new HashMap<>();
        Material cMaterial;
        int value;
        if (map != null)
            for (String sMaterial : map.keySet()) {
                try {
                    cMaterial = Material.matchMaterial(sMaterial);
                    if (cMaterial == null || cMaterial.isLegacy()) {
                        TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.trees.material-missing", "Cannot find modern material '{material}' for tree level.",
                                "{material}", sMaterial)));
                        continue;
                    }
                    Object rawValue = map.get(sMaterial);
                    if (!(rawValue instanceof Number)) {
                        TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.trees.material-numeric-required", "Tree level material '{material}' must use a numeric amount.",
                                "{material}", sMaterial)));
                        continue;
                    }
                    value = ((Number) rawValue).intValue();
                    levelupRequirements.put(cMaterial, value);
                } catch (IllegalArgumentException e) {
                    TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.trees.material-load-failed", "Cannot load material '{material}' for tree level.",
                            "{material}", sMaterial)));
                }
            }
        return levelupRequirements;
    }

    private static boolean shouldBackfillLegacyRequirements(TreeLevel level, Map<Material, Integer> savedRequirements) {
        if (level == null
                || !level.hasNext()
                || !"magic_tree".equalsIgnoreCase(level.getLevelName())
                || level.getLevelupRequirements() == null
                || level.getLevelupRequirements().isEmpty()) {
            return false;
        }
        if (savedRequirements == null || savedRequirements.isEmpty()) {
            return true;
        }
        for (Material material : savedRequirements.keySet()) {
            if (!level.getLevelupRequirements().containsKey(material)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesSavedWorld(World world, String savedWorldName) {
        if (world == null || savedWorldName == null || savedWorldName.isBlank()) {
            return false;
        }
        if (world.getName().equalsIgnoreCase(savedWorldName)) {
            return true;
        }

        String configuredWorldName = getWorldAlias(savedWorldName);
        if (configuredWorldName != null && world.getName().equalsIgnoreCase(configuredWorldName)) {
            logWorldAliasMapping(savedWorldName, world.getName());
            return true;
        }
        return false;
    }

    private static String getWorldAlias(String savedWorldName) {
        if (!(Main.getInstance() instanceof Main plugin)) {
            return null;
        }
        return plugin.getConfig().getString("migration.world-aliases." + savedWorldName);
    }

    private static void logWorldAliasMapping(String savedWorldName, String worldName) {
        String mapping = savedWorldName + "->" + worldName;
        if (loggedWorldAliasMappings.add(mapping)) {
            Main.getInstance().getLogger().info(LocaleManager.text("console.trees.world-alias", "Loading legacy X-Mas trees from saved world '{source}' into '{target}' via migration.world-aliases.",
                    "{source}", savedWorldName,
                    "{target}", worldName));
        }
    }

    private static void validateTreeId(String treeKey, Set<String> invalidTreeIds) {
        try {
            UUID.fromString(treeKey);
        } catch (IllegalArgumentException exception) {
            invalidTreeIds.add(treeKey);
        }
    }

    private static void validateUuid(String rawUuid, String treeKey, Set<String> invalidOwners) {
        try {
            UUID.fromString(rawUuid);
        } catch (IllegalArgumentException | NullPointerException exception) {
            invalidOwners.add(treeKey);
        }
    }

    private static void validateLevel(String levelName, String treeKey, Set<String> invalidLevels) {
        try {
            TreeLevel.fromString(levelName);
        } catch (IllegalArgumentException exception) {
            invalidLevels.add(treeKey + "=" + levelName);
        }
    }

    private static void validateLocation(FileConfiguration data, String treeKey, Set<String> missingWorlds, Set<String> invalidLocations,
                                         Set<String> duplicateLocations, Set<String> seenLocations) {
        String basePath = "trees." + treeKey + ".loc";
        String savedWorldName = data.getString(basePath + ".world");
        String resolvedWorldName = resolveWorldName(savedWorldName);
        if (savedWorldName == null || savedWorldName.isBlank()) {
            invalidLocations.add(treeKey + "=missing-world");
            return;
        }
        if (resolvedWorldName == null) {
            missingWorlds.add(savedWorldName);
        }
        if (!data.isSet(basePath + ".x") || !data.isSet(basePath + ".y") || !data.isSet(basePath + ".z")) {
            invalidLocations.add(treeKey + "=missing-coordinate");
            return;
        }

        int x = data.getInt(basePath + ".x");
        int y = data.getInt(basePath + ".y");
        int z = data.getInt(basePath + ".z");
        if (y < -64 || y > 512) {
            invalidLocations.add(treeKey + "=y:" + y);
        }

        String locationKey = (resolvedWorldName != null ? resolvedWorldName : savedWorldName).toLowerCase() + ":" + x + ":" + y + ":" + z;
        if (!seenLocations.add(locationKey)) {
            duplicateLocations.add(locationKey);
        }
    }

    private static String resolveWorldName(String savedWorldName) {
        if (savedWorldName == null || savedWorldName.isBlank()) {
            return null;
        }
        World exactWorld = Bukkit.getWorld(savedWorldName);
        if (exactWorld != null) {
            return exactWorld.getName();
        }
        String aliasWorldName = getWorldAlias(savedWorldName);
        if (aliasWorldName == null || aliasWorldName.isBlank()) {
            return null;
        }
        World aliasWorld = Bukkit.getWorld(aliasWorldName);
        return aliasWorld != null ? aliasWorld.getName() : null;
    }

    private static void validateRequirements(ConfigurationSection section, String treeKey, Set<String> invalidRequirements) {
        if (section == null) {
            return;
        }
        for (String materialName : section.getKeys(false)) {
            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isLegacy()) {
                invalidRequirements.add(treeKey + "=" + materialName);
                continue;
            }
            Object amount = section.get(materialName);
            if (!(amount instanceof Number) || ((Number) amount).intValue() < 0) {
                invalidRequirements.add(treeKey + "=" + materialName + ":" + amount);
            }
        }
    }
}
