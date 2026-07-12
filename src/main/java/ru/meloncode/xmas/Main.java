package ru.meloncode.xmas;

import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.HeadProfileUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private static final String CONFIG_RESOURCE_PATH = "config.yml";
    private static final String TREES_RESOURCE_PATH = "trees.yml";

    // Yeah. That's as it should be.
    static final Random RANDOM = new Random(Calendar.getInstance().get(Calendar.YEAR));
    static List<GiftEntry> gifts;
    static float LUCK_CHANCE;
    static boolean LUCK_CHANCE_ENABLED;
    static boolean resourceBack;
    static int MAX_TREE_COUNT;
    static boolean autoEnd;
    static boolean particlesEnabled;
    static float growFirstSoundVolume;
    static float growRepeatSoundVolume;
    static long endTime;
    static boolean inProgress;
    private static int UPDATE_SPEED;
    private static int PARTICLES_DELAY;
    private static NamespacedKey crystalKey;
    private static NamespacedKey noDamageFireworkKey;
    private static NamespacedKey presentHeadKey;
    private static NamespacedKey crystalRecipeKey;
    private static List<String> heads;
    private static Plugin plugin;
    private static final int MAX_SERIALIZED_GIFT_LENGTH = 65536;
    private FileConfiguration config;
    private String locale;
    private XMasPlaceholderExpansion placeholderExpansion;
    private ConfigDiagnostics configDiagnostics = ConfigDiagnostics.empty();
    private EventProgress eventProgress;

    public record ReloadSummary(
            String locale,
            int giftCount,
            int presentHeadCount,
            int configuredPresentHeadCount,
            int texturePresentHeadCount,
            int legacyPlayerHeadCount,
            int invalidPresentHeadCount,
            int invalidGiftCount,
            int configWarningCount,
            int treeDataWarningCount,
            int treeCount,
            int treeOwnerCount,
            boolean particlesEnabled,
            boolean resourceBack,
            float growFirstSoundVolume,
            float growRepeatSoundVolume,
            List<String> warnings
    ) {
    }

    public record ConfigDiagnostics(
            int configuredPresentHeadCount,
            int validPresentHeadCount,
            int texturePresentHeadCount,
            int legacyPlayerHeadCount,
            int invalidPresentHeadCount,
            int configuredGiftCount,
            int validGiftCount,
            int invalidGiftCount,
            int treeDataWarningCount,
            List<String> warnings
    ) {
        public static ConfigDiagnostics empty() {
            return new ConfigDiagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, Collections.emptyList());
        }

        public int configWarningCount() {
            return warnings.size();
        }
    }

    public record GiftEntry(ItemStack item, int weight) {
        public GiftEntry {
            item = item.clone();
            weight = Math.max(1, weight);
        }

        public ItemStack itemClone() {
            return item.clone();
        }
    }

    public static Plugin getInstance() {
        return plugin;
    }

    public static List<String> getHeads() {
        return heads;
    }

    public static NamespacedKey getCrystalKey() {
        return crystalKey;
    }

    public static NamespacedKey getNoDamageFireworkKey() {
        return noDamageFireworkKey;
    }

    public static NamespacedKey getPresentHeadKey() {
        return presentHeadKey;
    }

    public static int getUpdateSpeed() {
        return UPDATE_SPEED;
    }

    public EventProgress getEventProgress() {
        return eventProgress;
    }

    private File getPluginConfigFile() {
        return new File(getDataFolder(), CONFIG_RESOURCE_PATH);
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            config = ConfigUtils.loadManagedConfig(this, CONFIG_RESOURCE_PATH, getPluginConfigFile());
        }
        return config;
    }

    @Override
    public void reloadConfig() {
        config = ConfigUtils.loadManagedConfig(this, CONFIG_RESOURCE_PATH, getPluginConfigFile());
    }

    @Override
    public void saveConfig() {
        if (config == null) {
            return;
        }
        ConfigUtils.synchronizeWithResource(this, CONFIG_RESOURCE_PATH, config);
        ConfigUtils.saveConfig(getPluginConfigFile(), config);
    }

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaults();
        crystalKey = new NamespacedKey(this, "xmas_crystal");
        noDamageFireworkKey = new NamespacedKey(this, "no_damage_firework");
        presentHeadKey = new NamespacedKey(this, "present_head");
        crystalRecipeKey = new NamespacedKey(this, "xmas");
        config = getConfig();
        locale = config.getString("core.locale", LocaleManager.DEFAULT_LOCALE_CODE);
        LocaleManager.loadLocale(locale);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");
        inProgress = config.getBoolean("core.plugin-enabled", true);
        UPDATE_SPEED = config.getInt("core.update-speed");
        if (UPDATE_SPEED <= 0) {
            TextUtils.sendConsoleMessage(TextUtils.warning(LocaleManager.text("console.config.update-speed-invalid", "Update speed must be > 0")));
            TextUtils.sendConsoleMessage(TextUtils.muted(LocaleManager.text("console.config.update-speed-reset", "Setting value to default")));
            config.set("core.update-speed", 7);
            UPDATE_SPEED = 7;
        }
        PARTICLES_DELAY = config.getInt("core.particles-delay");
        if (PARTICLES_DELAY <= 0) {
            config.set("core.particles-delay", 35);
            PARTICLES_DELAY = 35;
        }
        particlesEnabled = config.getBoolean("core.particles-enabled", true);
        loadSoundConfig();
        
        autoEnd = config.getBoolean("core.holiday-ends.enabled");
        resourceBack = config.getBoolean("core.holiday-ends.resource-back");
        MAX_TREE_COUNT = config.getInt("core.tree-limit");
        Date date;
        try {
            date = sdf.parse(config.getString("core.holiday-ends.date"));
            endTime = date.getTime();
        } catch (ParseException e1) {
            TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.config.unable-load-holiday-end-date", "Unable to load holiday end date")));
        }
        defineTreeLevels();
        eventProgress = new EventProgress(this);
        for (World world : getServer().getWorlds()) {
            TreeSerializer.loadTrees(this, world);
        }

        loadDiagnosticsAndConfiguredItems(true);
        if (heads.size() == 0) {
            getLogger().warning(LocaleManager.text("console.gifts.no-heads", "Warning! No heads loaded. Presents cannot spawn without a box head."));
            return;
        }
        if (gifts.size() == 0) {
            getLogger().warning(LocaleManager.text("console.gifts.no-gifts", "Warning! No gifts loaded. No X-Mas without gifts."));
            return;
        }

        LUCK_CHANCE_ENABLED = config.getBoolean("xmas.luck.enabled");
        LUCK_CHANCE = (float) config.getInt("xmas.luck.chance") / 100;
        new Events().registerListener();
        new MagicTask(this).runTaskTimer(this, 5, UPDATE_SPEED);
        new PlayParticlesTask(this).runTaskTimer(this, 5, PARTICLES_DELAY);
        refreshCrystalItem();
        registerCrystalRecipe();
        XMasCommand.register(this);
        registerPlaceholderApi();
        TextUtils.sendConsoleMessage(LocaleManager.PLUGIN_ENABLED);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        TreeSerializer.loadTrees(this, event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (MagicTree magicTree : XMas.getAllTrees()) {
            if (magicTree.getLocation().getWorld() == event.getWorld()) magicTree.unbuild(false);
        }
    }

    public ReloadSummary reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        locale = config.getString("core.locale", LocaleManager.DEFAULT_LOCALE_CODE);
        LocaleManager.loadLocale(locale);

        inProgress = config.getBoolean("core.plugin-enabled", true);
        UPDATE_SPEED = config.getInt("core.update-speed");
        if (UPDATE_SPEED <= 0) {
            UPDATE_SPEED = 7;
        }
        PARTICLES_DELAY = config.getInt("core.particles-delay");
        if (PARTICLES_DELAY <= 0) {
            PARTICLES_DELAY = 35;
        }
        particlesEnabled = config.getBoolean("core.particles-enabled", true);
        loadSoundConfig();

        autoEnd = config.getBoolean("core.holiday-ends.enabled");
        resourceBack = config.getBoolean("core.holiday-ends.resource-back");
        MAX_TREE_COUNT = config.getInt("core.tree-limit");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");
            endTime = sdf.parse(config.getString("core.holiday-ends.date")).getTime();
        } catch (ParseException e) {
            TextUtils.sendConsoleMessage(TextUtils.error(LocaleManager.text("console.config.invalid-holiday-end-date", "Invalid holiday end date in config.yml")));
        }

        defineTreeLevels();
        refreshLoadedTreeDefinitions();
        if (eventProgress == null) {
            eventProgress = new EventProgress(this);
        } else {
            eventProgress.reload();
        }
        loadDiagnosticsAndConfiguredItems(true);

        LUCK_CHANCE_ENABLED = config.getBoolean("xmas.luck.enabled");
        LUCK_CHANCE = (float) config.getInt("xmas.luck.chance") / 100;
        refreshCrystalItem();
        registerCrystalRecipe();
        XMasCommand.refreshCommandConfiguration(this);
        return createReloadSummary();
    }

    public ReloadSummary createReloadSummary() {
        Set<UUID> owners = new HashSet<>();
        for (MagicTree tree : XMas.getAllTrees()) {
            owners.add(tree.getOwner());
        }
        return new ReloadSummary(
                LocaleManager.getActiveLocaleCode(),
                gifts != null ? gifts.size() : 0,
                heads != null ? heads.size() : 0,
                configDiagnostics.configuredPresentHeadCount(),
                configDiagnostics.texturePresentHeadCount(),
                configDiagnostics.legacyPlayerHeadCount(),
                configDiagnostics.invalidPresentHeadCount(),
                configDiagnostics.invalidGiftCount(),
                configDiagnostics.configWarningCount(),
                configDiagnostics.treeDataWarningCount(),
                XMas.getAllTrees().size(),
                owners.size(),
                particlesEnabled,
                resourceBack,
                growFirstSoundVolume,
                growRepeatSoundVolume,
                configDiagnostics.warnings()
        );
    }

    public ConfigDiagnostics getConfigDiagnostics() {
        return configDiagnostics;
    }

    private void loadSoundConfig() {
        growFirstSoundVolume = clampVolume(config.getDouble("core.sounds.grow.first-volume", 0.5));
        growRepeatSoundVolume = clampVolume(config.getDouble("core.sounds.grow.repeat-volume", 0.2));
    }

    private void refreshLoadedTreeDefinitions() {
        for (MagicTree tree : XMas.getAllTrees()) {
            tree.refreshLevelDefinition();
        }
    }

    private float clampVolume(double volume) {
        if (Double.isNaN(volume)) {
            return 0;
        }
        return (float) Math.max(0, Math.min(1, volume));
    }

    public void addGiftItem(ItemStack item) {
        ItemStack gift = item.clone();
        String serializedItem = serializeItem(gift);
        if (serializedItem == null) {
            getLogger().warning(LocaleManager.text("console.gifts.serialize-list-item-failed", "Failed to serialize item for saving to the gift list. Item: {item}",
                    "{item}", gift.toString()));
            return;
        }

        List<Object> giftList = getRawGiftList();
        giftList.add(serializedItem);
        config.set("xmas.gifts", giftList);
        saveConfig();
        reloadGiftItemsFromConfig();
    }

    public List<ItemStack> getGiftItems() {
        List<ItemStack> snapshot = new ArrayList<>();
        if (gifts == null) {
            return snapshot;
        }
        for (GiftEntry gift : gifts) {
            if (gift != null && gift.item() != null) {
                snapshot.add(gift.itemClone());
            }
        }
        return snapshot;
    }

    public List<GiftEntry> getGiftEntries() {
        List<GiftEntry> snapshot = new ArrayList<>();
        if (gifts == null) {
            return snapshot;
        }
        for (GiftEntry gift : gifts) {
            if (gift != null && gift.item() != null) {
                snapshot.add(new GiftEntry(gift.item(), gift.weight()));
            }
        }
        return snapshot;
    }

    public ItemStack rollGiftItem() {
        return rollConfiguredGift();
    }

    public static ItemStack rollConfiguredGift() {
        if (gifts == null || gifts.isEmpty()) {
            return null;
        }
        int totalWeight = getGiftTotalWeight();
        if (totalWeight <= 0) {
            return null;
        }
        int roll = RANDOM.nextInt(totalWeight) + 1;
        int runningWeight = 0;
        for (GiftEntry gift : gifts) {
            runningWeight += gift.weight();
            if (roll <= runningWeight) {
                return gift.itemClone();
            }
        }
        return gifts.get(gifts.size() - 1).itemClone();
    }

    public static int getGiftTotalWeight() {
        if (gifts == null || gifts.isEmpty()) {
            return 0;
        }
        int totalWeight = 0;
        for (GiftEntry gift : gifts) {
            if (gift != null) {
                totalWeight += Math.max(1, gift.weight());
            }
        }
        return totalWeight;
    }

    public ItemStack removeGiftItem(int displayIndex) {
        if (displayIndex < 1) {
            return null;
        }

        List<Object> giftList = getRawGiftList();
        int validIndex = 0;
        for (int rawIndex = 0; rawIndex < giftList.size(); rawIndex++) {
            GiftEntry gift = deserializeGiftEntry(giftList.get(rawIndex), true);
            if (gift == null) {
                continue;
            }
            validIndex++;
            if (validIndex == displayIndex) {
                giftList.remove(rawIndex);
                config.set("xmas.gifts", giftList);
                saveConfig();
                reloadGiftItemsFromConfig();
                return gift.itemClone();
            }
        }
        return null;
    }

    public GiftEntry updateGiftWeight(int displayIndex, int weight) {
        if (displayIndex < 1 || weight < 1) {
            return null;
        }

        List<Object> giftList = getRawGiftList();
        int validIndex = 0;
        for (int rawIndex = 0; rawIndex < giftList.size(); rawIndex++) {
            Object rawGift = giftList.get(rawIndex);
            GiftEntry gift = deserializeGiftEntry(rawGift, true);
            if (gift == null) {
                continue;
            }
            validIndex++;
            if (validIndex == displayIndex) {
                Map<String, Object> updatedGift = new LinkedHashMap<>();
                updatedGift.put("item", extractGiftItemValue(rawGift));
                updatedGift.put("weight", weight);
                giftList.set(rawIndex, updatedGift);
                config.set("xmas.gifts", giftList);
                saveConfig();
                reloadGiftItemsFromConfig();
                return new GiftEntry(gift.item(), weight);
            }
        }
        return null;
    }

    private void reloadGiftItemsFromConfig() {
        gifts = new ArrayList<>();
        for (Object configuredGift : getRawGiftList()) {
            GiftEntry gift = deserializeGiftEntry(configuredGift, false);
            if (gift != null) {
                gifts.add(gift);
            }
        }
    }

    private void loadDiagnosticsAndConfiguredItems(boolean logWarnings) {
        List<String> warnings = new ArrayList<>();
        List<String> configuredHeads = config.getStringList("xmas.presents");
        List<String> validHeads = new ArrayList<>();
        int textureHeads = 0;
        int legacyPlayerHeads = 0;
        int invalidHeads = 0;

        for (String configuredHead : configuredHeads) {
            HeadProfileUtils.HeadConfigValidation validation = HeadProfileUtils.validateConfiguredHead(configuredHead);
            if (validation.valid()) {
                validHeads.add(configuredHead.trim());
                if (validation.type() == HeadProfileUtils.HeadConfigType.TEXTURE_URL) {
                    textureHeads++;
                } else if (validation.type() == HeadProfileUtils.HeadConfigType.PLAYER_NAME) {
                    legacyPlayerHeads++;
                }
            } else {
                invalidHeads++;
                if (validation.warning() != null) {
                    warnings.add(validation.warning());
                }
            }
        }
        if (legacyPlayerHeads > 0) {
            warnings.add(LocaleManager.text("console.heads.legacy-player-names", "Present heads include {count} legacy player-name entr{plural}. Texture URLs are preferred for predictable Paper 26.x behavior.",
                    "{count}", Integer.toString(legacyPlayerHeads),
                    "{plural}", legacyPlayerHeads == 1 ? "y" : "ies"));
        }

        List<Object> configuredGifts = getRawGiftList();
        List<GiftEntry> validGifts = new ArrayList<>();
        int invalidGifts = 0;
        for (Object configuredGift : configuredGifts) {
            GiftEntry gift = deserializeGiftEntry(configuredGift, false);
            if (gift != null) {
                validGifts.add(gift);
            } else {
                invalidGifts++;
                warnings.add(LocaleManager.text("console.gifts.invalid-entry", "Invalid gift entry skipped: {item}",
                        "{item}", abbreviate(String.valueOf(configuredGift), 80)));
            }
        }

        TreeSerializer.TreeDataValidationReport treeDataReport = TreeSerializer.validateTreesFile();
        int treeDataWarningCount = treeDataReport.warningCount();
        if (treeDataWarningCount > 0) {
            warnings.add(LocaleManager.text("console.trees.validation-warnings", "trees.yml has {count} validation warning(s). Run /xmastree data validate for details.",
                    "{count}", Integer.toString(treeDataWarningCount)));
        }

        if (validHeads.isEmpty()) {
            warnings.add(LocaleManager.text("console.gifts.no-valid-heads", "No valid present head entries are configured."));
        }
        if (validGifts.isEmpty()) {
            warnings.add(LocaleManager.text("console.gifts.no-valid-gifts", "No valid gift entries are configured."));
        }

        heads = validHeads;
        gifts = validGifts;
        configDiagnostics = new ConfigDiagnostics(
                configuredHeads.size(),
                validHeads.size(),
                textureHeads,
                legacyPlayerHeads,
                invalidHeads,
                configuredGifts.size(),
                validGifts.size(),
                invalidGifts,
                treeDataWarningCount,
                Collections.unmodifiableList(new ArrayList<>(warnings))
        );

        if (logWarnings) {
            for (String warning : warnings) {
                getLogger().warning(warning);
            }
        }
    }

    private List<Object> getRawGiftList() {
        List<?> rawList = config.getList("xmas.gifts", Collections.emptyList());
        return new ArrayList<>(rawList);
    }

    private static GiftEntry deserializeGiftEntry(Object configuredGift, boolean logWarnings) {
        if (configuredGift instanceof Map<?, ?> map) {
            Object itemValue = firstPresentValue(map, "item", "reward", "material", "value");
            if (itemValue == null) {
                return null;
            }
            int weight = parseGiftWeight(map.get("weight"));
            if (weight < 1) {
                return null;
            }
            ItemStack item = deserializeItem(String.valueOf(itemValue), logWarnings);
            return item != null ? new GiftEntry(item, weight) : null;
        }

        ItemStack item = deserializeItem(String.valueOf(configuredGift), logWarnings);
        return item != null ? new GiftEntry(item, 1) : null;
    }

    private static Object firstPresentValue(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    private static int parseGiftWeight(Object rawWeight) {
        if (rawWeight == null) {
            return 1;
        }
        if (rawWeight instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(rawWeight).trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String extractGiftItemValue(Object rawGift) {
        if (rawGift instanceof Map<?, ?> map) {
            Object itemValue = firstPresentValue(map, "item", "reward", "material", "value");
            if (itemValue != null) {
                return String.valueOf(itemValue);
            }
        }
        return String.valueOf(rawGift);
    }

    private String serializeItem(ItemStack item) {
        try {
            byte[] serializedBytes = item.serializeAsBytes();
            return Base64.getEncoder().encodeToString(serializedBytes);
        } catch (Exception e) {
            getLogger().severe(LocaleManager.text("console.gifts.serialize-item-failed", "Failed to serialize item: {error}",
                    "{error}", e.getMessage()));
            return null;
        }
    }

    public static ItemStack deserializeItem(String serializedItem) {
        return deserializeItem(serializedItem, true);
    }

    private static ItemStack deserializeItem(String serializedItem, boolean logWarnings) {
        if (serializedItem == null) {
            return null;
        }
        String trimmed = serializedItem.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        ItemStack materialItem = deserializeMaterialItem(trimmed);
        if (materialItem != null) {
            return materialItem;
        }

        if (trimmed.length() > MAX_SERIALIZED_GIFT_LENGTH) {
            if (logWarnings) {
                Bukkit.getLogger().severe(LocaleManager.text("console.gifts.too-large", "Gift item is too large to deserialize safely: {length} characters",
                        "{length}", Integer.toString(trimmed.length())));
            }
            return null;
        }

        try {
            byte[] serializedBytes = Base64.getDecoder().decode(trimmed);
            return ItemStack.deserializeBytes(serializedBytes);
        } catch (IllegalArgumentException e) {
            if (logWarnings) {
                Bukkit.getLogger().severe(LocaleManager.text("console.gifts.invalid-material-or-base64", "Invalid material name or Base64 gift item: {item}",
                        "{item}", trimmed));
            }
            return null;
        } catch (Exception e) {
            if (logWarnings) {
                Bukkit.getLogger().severe(LocaleManager.text("console.gifts.deserialize-error", "Failed to deserialize gift item: {error}",
                        "{error}", e.getMessage()));
            }
            return null;
        }
    }

    private static String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static ItemStack deserializeMaterialItem(String serializedItem) {
        String materialName = serializedItem;
        int amount = 1;
        if (serializedItem.contains(":")) {
            String[] split = serializedItem.split(":", 2);
            materialName = split[0].trim();
            try {
                amount = Integer.parseInt(split[1].trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null || material.isLegacy() || !material.isItem()) {
            return null;
        }
        int maxStackSize = Math.max(1, material.getMaxStackSize());
        amount = Math.max(1, Math.min(amount, maxStackSize));
        return new ItemStack(material, amount);
    }


    @Override
    public void onDisable() {
        if (placeholderExpansion != null && placeholderExpansion.isRegistered()) {
            placeholderExpansion.unregister();
        }
        if (eventProgress != null) {
            eventProgress.save();
        }
        if (XMas.getAllTrees().size() > 0)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.unbuild(false);
            }
    }

    private void registerPlaceholderApi() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return;
        }
        placeholderExpansion = new XMasPlaceholderExpansion(this);
        if (placeholderExpansion.register()) {
            getLogger().info(LocaleManager.text("console.placeholder.registered", "Registered PlaceholderAPI expansion: {identifier}",
                    "{identifier}", XMasPlaceholders.IDENTIFIER));
        } else {
            getLogger().warning(LocaleManager.text("console.placeholder.registration-failed", "PlaceholderAPI is present, but placeholder registration failed."));
        }
    }

    public void end() {
        Bukkit.broadcast(TextUtils.parse(TextUtils.accent(LocaleManager.HAPPY_NEW_YEAR)));
        inProgress = false;
        config.set("core.plugin-enabled", false);
        saveConfig();
    }

    private void saveDefaults() {
        reloadConfig();
        File treesFile = new File(getDataFolder(), TREES_RESOURCE_PATH);
        if (!treesFile.exists()) {
            plugin.saveResource(TREES_RESOURCE_PATH, false);
        }
    }

    private void refreshCrystalItem() {
        XMas.XMAS_CRYSTAL = new ItemMaker(Material.EMERALD, LocaleManager.CRYSTAL_NAME, LocaleManager.CRYSTAL_LORE).make();
        ItemMeta crystalMeta = XMas.XMAS_CRYSTAL.getItemMeta();
        if (crystalMeta != null) {
            crystalMeta.getPersistentDataContainer().set(crystalKey, PersistentDataType.BYTE, (byte) 1);
            XMas.XMAS_CRYSTAL.setItemMeta(crystalMeta);
        }
    }

    private void registerCrystalRecipe() {
        getServer().removeRecipe(crystalRecipeKey);
        ShapedRecipe grinderRecipe = new ShapedRecipe(crystalRecipeKey, XMas.XMAS_CRYSTAL)
                .shape(" d ", "ded", " d ")
                .setIngredient('d', Material.DIAMOND)
                .setIngredient('e', Material.EMERALD);
        try {
            getServer().addRecipe(grinderRecipe);
        } catch (Exception ignored) {
        }
    }

    private void defineTreeLevels() {

        long sapling_delay = config.getInt("xmas.tree-lvl.sapling.gift-cooldown") * 20 / UPDATE_SPEED;
        long small_delay = config.getInt("xmas.tree-lvl.small_tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long tree_delay = config.getInt("xmas.tree-lvl.tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long magic_delay = config.getInt("xmas.tree-lvl.magic_tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long super_delay = config.getInt("xmas.tree-lvl.super_tree.gift-cooldown") * 20 / UPDATE_SPEED;

        ConfigurationSection lvlups = config.getConfigurationSection("xmas.tree-lvl");
        Map<Material, Integer> saplingLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("sapling.lvlup").getValues(false));
        Map<Material, Integer> smallLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("small_tree.lvlup").getValues(false));
        Map<Material, Integer> treeLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("tree.lvlup").getValues(false));
        Map<Material, Integer> magicLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("magic_tree.lvlup").getValues(false));

        TreeLevel.SUPER_TREE = new TreeLevel("super_tree",
                getParticleEffect("super_tree", "ambient", Effects.TREE_WHITE_AMBIENT),
                getParticleEffect("super_tree", "swag", Effects.TREE_GOLD_SWAG),
                getParticleEffect("super_tree", "body", null),
                null, super_delay, Collections.emptyMap(), new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                for (int i = 0; i <= 7; i++) {
                    put(new Vector(0, i, 0), Material.SPRUCE_LOG);
                    if (i >= 2) {
                        put(new Vector(1, i, 0), Material.SPRUCE_LEAVES);
                        put(new Vector(-1, i, 0), Material.SPRUCE_LEAVES);
                        put(new Vector(0, i, 1), Material.SPRUCE_LEAVES);
                        put(new Vector(0, i, -1), Material.SPRUCE_LEAVES);
                    }
                }
                put(new Vector(0, 8, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 9, 0), Material.GLOWSTONE);

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) + Math.abs(z) <= 3 && !(x == 0 && z == 0)) {
                            put(new Vector(x, 2, z), Material.SPRUCE_LEAVES);
                        }
                    }
                }
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) + Math.abs(z) <= 2 && !(x == 0 && z == 0)) {
                            put(new Vector(x, 4, z), Material.SPRUCE_LEAVES);
                        }
                    }
                }
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (!(x == 0 && z == 0)) {
                            put(new Vector(x, 6, z), Material.SPRUCE_LEAVES);
                        }
                    }
                }

                put(new Vector(2, 3, 0), Material.REDSTONE_BLOCK);
                put(new Vector(-2, 3, 0), Material.GOLD_BLOCK);
                put(new Vector(0, 5, 2), Material.EMERALD_BLOCK);
                put(new Vector(0, 5, -2), Material.DIAMOND_BLOCK);
            }
        }));

        TreeLevel.MAGIC_TREE = new TreeLevel("magic_tree",
                getParticleEffect("magic_tree", "ambient", Effects.TREE_WHITE_AMBIENT),
                getParticleEffect("magic_tree", "swag", Effects.TREE_SWAG),
                getParticleEffect("magic_tree", "body", null),
                TreeLevel.SUPER_TREE, magic_delay, magicLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                for (int i = 0; i <= 5; i++) {
                    put(new Vector(0, i, 0), Material.SPRUCE_LOG);
                    if (i >= 2) {
                        put(new Vector(1, i, 0), Material.SPRUCE_LEAVES);
                        put(new Vector(-1, i, 0), Material.SPRUCE_LEAVES);
                        put(new Vector(0, i, 1), Material.SPRUCE_LEAVES);
                        put(new Vector(0, i, -1), Material.SPRUCE_LEAVES);
                    }
                }
                put(new Vector(0, 6, 0), Material.SPRUCE_LEAVES);

                put(new Vector(0, 7, 0), Material.GLOWSTONE);// Star

                put(new Vector(1, 4, 0), Material.SPRUCE_LEAVES);
                put(new Vector(1, 4, 1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 4, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 4, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 4, 1), Material.SPRUCE_LEAVES);

                put(new Vector(1, 2, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 2, -1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 2, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 2, 1), Material.SPRUCE_LEAVES);

                put(new Vector(2, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, 2), Material.SPRUCE_LEAVES);
                put(new Vector(-2, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, -2), Material.SPRUCE_LEAVES);
            }
        }));

        TreeLevel.TREE = new TreeLevel("tree",
                getParticleEffect("tree", "ambient", Effects.AMBIENT_SNOW),
                getParticleEffect("tree", "swag", Effects.TREE_GOLD_SWAG),
                getParticleEffect("tree", "body", null),
                TreeLevel.MAGIC_TREE, tree_delay, treeLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                put(new Vector(0, 0, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 1, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 2, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 3, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 4, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 5, 0), Material.SPRUCE_LEAVES);
                put(new Vector(1, 4, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 4, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 4, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 4, -1), Material.SPRUCE_LEAVES);

                put(new Vector(1, 1, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 1, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 1, -1), Material.SPRUCE_LEAVES);

                put(new Vector(1, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, -1), Material.SPRUCE_LEAVES);

            }
        }));

        TreeLevel.SMALL_TREE = new TreeLevel("small_tree",
                getParticleEffect("small_tree", "ambient", Effects.AMBIENT_PORTAL),
                getParticleEffect("small_tree", "swag", Effects.TREE_RED_SWAG),
                getParticleEffect("small_tree", "body", null),
                TreeLevel.TREE, small_delay, smallLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                put(new Vector(0, 0, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 1, 0), Material.SPRUCE_LOG);
                put(new Vector(0, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 3, 0), Material.SPRUCE_LEAVES);

                put(new Vector(1, 1, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 1, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, -1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 1, 1), Material.SPRUCE_LEAVES);
                put(new Vector(1, 1, -1), Material.SPRUCE_LEAVES);

                put(new Vector(1, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, 1), Material.SPRUCE_LEAVES);
                put(new Vector(-1, 2, 0), Material.SPRUCE_LEAVES);
                put(new Vector(0, 2, -1), Material.SPRUCE_LEAVES);

            }
        }));

        TreeLevel.SAPLING = new TreeLevel("sapling",
                getParticleEffect("sapling", "ambient", Effects.AMBIENT_SAPLING),
                getParticleEffect("sapling", "swag", null),
                getParticleEffect("sapling", "body", null),
                TreeLevel.SMALL_TREE, sapling_delay, saplingLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                put(new Vector(0, 0, 0), Material.SPRUCE_SAPLING);
            }
        }));
    }

    private ParticleContainer getParticleEffect(String level, String effect, ParticleContainer fallback) {
        String path = "xmas.tree-lvl." + level + ".particles." + effect;
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return fallback;
        }
        if (!section.getBoolean("enabled", fallback != null)) {
            return null;
        }

        String configuredParticle = section.getString("particle", fallback != null ? fallback.getType().name() : null);
        if (configuredParticle == null || configuredParticle.trim().isEmpty()) {
            return fallback;
        }

        Particle particle;
        try {
            particle = Particle.valueOf(configuredParticle.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            getLogger().warning(LocaleManager.text("console.particles.unknown", "Unknown particle '{particle}' at {path}. Using fallback.",
                    "{particle}", configuredParticle,
                    "{path}", path));
            return fallback;
        }

        if (particle.getDataType() != Void.class && particle != Particle.DUST) {
            getLogger().warning(LocaleManager.text("console.particles.extra-data-unsupported", "Particle '{particle}' needs extra data and is not supported in config yet. Using fallback.",
                    "{particle}", particle.name()));
            return fallback;
        }

        return new ParticleContainer(
                particle,
                (float) section.getDouble("offset-x", fallback != null ? fallback.getOffsetX() : 0),
                (float) section.getDouble("offset-y", fallback != null ? fallback.getOffsetY() : 0),
                (float) section.getDouble("offset-z", fallback != null ? fallback.getOffsetZ() : 0),
                (float) section.getDouble("speed", fallback != null ? fallback.getSpeed() : 0),
                Math.max(0, section.getInt("count", fallback != null ? fallback.getCount() : 0))
        );
    }
}
