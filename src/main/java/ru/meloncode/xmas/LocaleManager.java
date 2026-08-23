package ru.meloncode.xmas;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LocaleManager {

    public static final String DEFAULT_LOCALE_CODE = "en";
    private static final String DEFAULT_LOCALE_RESOURCE_PATH = "translations/locale_en.yml";
    private static final String TRANSLATIONS_DIRECTORY = "translations";
    private static final String LEGACY_LOCALES_DIRECTORY = "locales";

    private static final String BOOTSTRAP_PLUGIN_NAME = "XMas Tree";
    private static final String BOOTSTRAP_PREFIX = "<xm-accent-2>[</xm-accent-2><xm-accent>{plugin_name}</xm-accent><xm-accent-2>]</xm-accent-2> <reset>";
    private static final String BOOTSTRAP_CONSOLE_PREFIX = "<xm-accent-2>[</xm-accent-2><xm-accent>{plugin_name}</xm-accent><xm-accent-2>]</xm-accent-2> <xm-muted>";

    private static final Map<String, String> BOOTSTRAP_THEME = createBootstrapTheme();

    public static String PLUGIN_NAME = BOOTSTRAP_PLUGIN_NAME;
    public static String PLUGIN_ENABLED;
    public static String GROW_LVL_PROGRESS;
    public static String GROW_LVL_READY;
    public static String GROW_LEVEL_MAX;
    public static String GROW_REQ_LIST_TITLE;
    public static String GROW_REQ_LIST_HINT;
    public static String GROW_NOT_ENOUGH_PLACE;
    public static String TREE_LIMIT;
    public static String DESTROY_SAPLING;
    public static String DESTROY_LEAVES_SANTA;
    public static String DESTROY_LEAVES_TUT;
    public static String DESTROY_WARNING;
    public static String DESTROY_RESOURCE_BACK;
    public static String DESTROY_FAIL_OWNER;
    public static String DESTROY_TUT;
    public static String DESTROY_COMPLETE;
    public static String CRYSTAL_NAME;
    public static List<String> CRYSTAL_LORE = new ArrayList<>();
    public static String GIFT_LUCK;
    public static String GIFT_FAIL;
    public static String TIMEOUT;
    public static String HAPPY_NEW_YEAR;

    public static List<String> COMMAND_HELP = new ArrayList<>();
    public static String COMMAND_PLAYER_OFFLINE;
    public static String COMMAND_NO_PLAYER_NAME;
    public static String COMMAND_GIVEAWAY;

    private static FileConfiguration defaultLocale;
    private static FileConfiguration bundledDefaultLocale;
    private static FileConfiguration locale;
    private static String activeLocaleCode = DEFAULT_LOCALE_CODE;
    private static String chatPrefix = BOOTSTRAP_PREFIX;
    private static String consolePrefix = BOOTSTRAP_CONSOLE_PREFIX;
    private static Map<String, String> themeAliases = buildThemeAliases(BOOTSTRAP_THEME);

    private LocaleManager() {
    }

    public static void loadLocale(String localeCode) {
        Main plugin = (Main) Main.getInstance();
        activeLocaleCode = normalizeLocaleCode(localeCode);

        File englishFile = getTranslationFile(plugin, DEFAULT_LOCALE_CODE);
        migrateLegacyLocaleIfNeeded(plugin, DEFAULT_LOCALE_CODE, englishFile);
        bundledDefaultLocale = ConfigUtils.loadResourceConfig(plugin, DEFAULT_LOCALE_RESOURCE_PATH);
        defaultLocale = ConfigUtils.loadManagedConfig(plugin, DEFAULT_LOCALE_RESOURCE_PATH, englishFile);

        File requestedFile = getTranslationFile(plugin, activeLocaleCode);
        if (!DEFAULT_LOCALE_CODE.equals(activeLocaleCode)) {
            migrateLegacyLocaleIfNeeded(plugin, activeLocaleCode, requestedFile);
        }

        if (DEFAULT_LOCALE_CODE.equals(activeLocaleCode)) {
            locale = defaultLocale;
        } else if (requestedFile.exists()) {
            YamlConfiguration requestedLocale = ConfigUtils.loadConfig(requestedFile);
            boolean changed = ConfigUtils.synchronizeWithDefaults(requestedLocale, defaultLocale);
            locale = requestedLocale;
            if (changed) {
                ConfigUtils.saveConfig(requestedFile, requestedLocale);
            }
        } else {
            locale = defaultLocale;
        }

        loadStrings();
        if (DEFAULT_LOCALE_CODE.equals(activeLocaleCode) || requestedFile.exists()) {
            TextUtils.sendConsoleMessage(TextUtils.success(text("console.translation.loaded", "Loaded translation '{file}'.").replace("{file}", requestedFile.getName())));
        } else {
            TextUtils.sendConsoleMessage(TextUtils.warning(text("console.translation.missing", "Translation '{file}' was not found.").replace("{file}", requestedFile.getName())));
            TextUtils.sendConsoleMessage(TextUtils.muted(text("console.translation.fallback", "Falling back to locale_en.yml.")));
        }
    }

    public static String text(String path) {
        return text(path, null);
    }

    public static String text(String path, String fallback) {
        String value = resolveString(locale, path);
        if (value != null) {
            return replaceCommonTokens(value);
        }

        value = resolveString(defaultLocale, path);
        if (value != null) {
            return replaceCommonTokens(value);
        }

        return fallback == null ? null : replaceCommonTokens(fallback);
    }

    public static String text(String path, String fallback, String... replacements) {
        String value = text(path, fallback);
        if (value == null || replacements == null) {
            return value;
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], String.valueOf(replacements[i + 1]));
        }
        return value;
    }

    public static List<String> textList(String path) {
        List<String> values = resolveStringList(locale, path);
        if (!values.isEmpty()) {
            return replaceCommonTokens(values);
        }

        values = resolveStringList(defaultLocale, path);
        if (!values.isEmpty()) {
            return replaceCommonTokens(values);
        }

        return new ArrayList<>();
    }

    public static List<String> bundledTextList(String path) {
        List<String> values = resolveStringList(bundledDefaultLocale, path);
        return replaceCommonTokens(values);
    }

    public static String replaceCommonTokens(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("{plugin_name}", PLUGIN_NAME != null ? PLUGIN_NAME : BOOTSTRAP_PLUGIN_NAME);
    }

    public static List<String> replaceCommonTokens(List<String> lines) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(replaceCommonTokens(line));
        }
        return replaced;
    }

    public static Map<String, String> getThemeAliases() {
        return themeAliases;
    }

    public static String getChatPrefix() {
        return replaceCommonTokens(chatPrefix);
    }

    public static String getConsolePrefix() {
        return replaceCommonTokens(consolePrefix);
    }

    public static String getActiveLocaleCode() {
        return activeLocaleCode;
    }

    private static void loadStrings() {
        PLUGIN_NAME = text("plugin-name", BOOTSTRAP_PLUGIN_NAME);
        chatPrefix = text("format.prefix", BOOTSTRAP_PREFIX);
        consolePrefix = text("format.console-prefix", BOOTSTRAP_CONSOLE_PREFIX);
        themeAliases = buildThemeAliases(readTheme());

        PLUGIN_ENABLED = text("messages.plugin-enabled");
        GROW_LVL_PROGRESS = text("messages.tree.grow-lvl-progress");
        GROW_LVL_READY = text("messages.tree.grow-lvl-ready");
        GROW_LEVEL_MAX = text("messages.tree.grow-lvl-max");
        GROW_REQ_LIST_TITLE = text("messages.tree.grow-req-list-title");
        GROW_REQ_LIST_HINT = text("messages.tree.grow-req-list-hint");
        GROW_NOT_ENOUGH_PLACE = text("messages.tree.grow-not-enough-place");
        TREE_LIMIT = text("messages.tree.tree-limit");
        DESTROY_SAPLING = text("messages.tree.destroy-sapling");
        DESTROY_LEAVES_SANTA = text("messages.tree.destroy-leaves-santa");
        DESTROY_LEAVES_TUT = text("messages.tree.destroy-leaves-tut");
        DESTROY_WARNING = text("messages.tree.destroy-warning");
        DESTROY_RESOURCE_BACK = text("messages.tree.destroy-resource-back");
        DESTROY_TUT = text("messages.tree.destroy-tut");
        DESTROY_COMPLETE = text("messages.tree.destroy-complete");
        DESTROY_FAIL_OWNER = text("messages.tree.destroy-fail-owner");
        CRYSTAL_NAME = text("crystal.name");
        CRYSTAL_LORE = textList("crystal.lore");
        GIFT_LUCK = text("messages.gift.luck-message");
        GIFT_FAIL = text("messages.gift.unluck-message");
        TIMEOUT = text("messages.timeout");
        HAPPY_NEW_YEAR = text("messages.final-wish");

        COMMAND_HELP = textList("command.help");
        COMMAND_PLAYER_OFFLINE = text("command.player-offline");
        COMMAND_NO_PLAYER_NAME = text("command.no-player-name");
        COMMAND_GIVEAWAY = text("command.giveaway");
    }

    private static Map<String, String> readTheme() {
        Map<String, String> theme = new LinkedHashMap<>(BOOTSTRAP_THEME);
        theme.put("xm-text", text("theme.text", BOOTSTRAP_THEME.get("xm-text")));
        theme.put("xm-muted", text("theme.muted", BOOTSTRAP_THEME.get("xm-muted")));
        theme.put("xm-accent", text("theme.accent", BOOTSTRAP_THEME.get("xm-accent")));
        theme.put("xm-accent-2", text("theme.accent-secondary", BOOTSTRAP_THEME.get("xm-accent-2")));
        theme.put("xm-label", text("theme.label", BOOTSTRAP_THEME.get("xm-label")));
        theme.put("xm-success", text("theme.success", BOOTSTRAP_THEME.get("xm-success")));
        theme.put("xm-warning", text("theme.warning", BOOTSTRAP_THEME.get("xm-warning")));
        theme.put("xm-error", text("theme.error", BOOTSTRAP_THEME.get("xm-error")));
        theme.put("xm-info", text("theme.info", BOOTSTRAP_THEME.get("xm-info")));
        theme.put("xm-command", text("theme.command", text("theme.accent-secondary", BOOTSTRAP_THEME.get("xm-command"))));
        return theme;
    }

    private static Map<String, String> buildThemeAliases(Map<String, String> theme) {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : theme.entrySet()) {
            aliases.put("<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
            aliases.put("</" + entry.getKey() + ">", "</" + entry.getValue() + ">");
        }
        return aliases;
    }

    private static String resolveString(FileConfiguration configuration, String path) {
        if (configuration == null || path == null) {
            return null;
        }

        String message = configuration.getString(path);
        if (message == null || message.contains("_UNUSED")) {
            return null;
        }
        return message;
    }

    private static List<String> resolveStringList(FileConfiguration configuration, String path) {
        List<String> list = new ArrayList<>();
        if (configuration == null || path == null) {
            return list;
        }

        List<String> raw = configuration.getStringList(path);
        if (raw == null) {
            return list;
        }
        for (String line : raw) {
            list.add(line);
        }
        return list;
    }

    private static File getTranslationFile(Main plugin, String localeCode) {
        return new File(plugin.getDataFolder(), TRANSLATIONS_DIRECTORY + "/locale_" + localeCode + ".yml");
    }

    private static String normalizeLocaleCode(String localeCode) {
        if (localeCode == null || localeCode.isBlank()) {
            return DEFAULT_LOCALE_CODE;
        }

        String normalized = localeCode.trim().toLowerCase();
        if (normalized.startsWith("locale_")) {
            normalized = normalized.substring("locale_".length());
        }
        if (normalized.endsWith(".yml")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        if (normalized.startsWith("translations/")) {
            normalized = normalized.substring("translations/".length());
        }
        return normalized.isBlank() ? DEFAULT_LOCALE_CODE : normalized;
    }

    private static void migrateLegacyLocaleIfNeeded(Main plugin, String localeCode, File targetFile) {
        if (targetFile.exists()) {
            return;
        }

        File legacyLocaleFile = new File(plugin.getDataFolder(), LEGACY_LOCALES_DIRECTORY + "/" + localeCode + ".yml");
        File legacyDefaultFile = new File(plugin.getDataFolder(), LEGACY_LOCALES_DIRECTORY + "/default.yml");
        File sourceFile = legacyLocaleFile.exists() ? legacyLocaleFile : (DEFAULT_LOCALE_CODE.equals(localeCode) && legacyDefaultFile.exists() ? legacyDefaultFile : null);

        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            plugin.getLogger().warning(text("console.translation.create-directory-failed", "Unable to create translations directory at {directory}",
                    "{directory}", parent.getPath()));
            return;
        }

        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info(text("console.translation.migrated-legacy", "Migrated legacy locale '{source}' to '{target}'.",
                    "{source}", sourceFile.getName(),
                    "{target}", targetFile.getPath()));
        } catch (IOException exception) {
            plugin.getLogger().warning(text("console.translation.migrate-legacy-failed", "Unable to migrate legacy locale '{source}' to '{target}': {error}",
                    "{source}", sourceFile.getPath(),
                    "{target}", targetFile.getPath(),
                    "{error}", exception.getMessage()));
        }
    }

    private static Map<String, String> createBootstrapTheme() {
        Map<String, String> theme = new LinkedHashMap<>();
        theme.put("xm-text", "#f7f1e8");
        theme.put("xm-muted", "#c7c0bb");
        theme.put("xm-accent", "#9fe3d6");
        theme.put("xm-accent-2", "#f4c2d7");
        theme.put("xm-label", "#f3d38f");
        theme.put("xm-success", "#b9e8b5");
        theme.put("xm-warning", "#f6d58b");
        theme.put("xm-error", "#f3a7a7");
        theme.put("xm-info", "#a9d4ff");
        theme.put("xm-command", "#f4c2d7");
        return theme;
    }
}
