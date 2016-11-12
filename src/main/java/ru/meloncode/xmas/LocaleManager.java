package ru.meloncode.xmas;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import ru.meloncode.xmas.utils.ConfigUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.util.List;

public class LocaleManager {

    public static String PLUGIN_NAME;
    public static String PLUGIN_ENABLED;
    public static String GROW_LVL_PROGRESS;
    public static String GROW_LVL_READY;
    public static String GROW_LEVEL_MAX;
    public static String GROW_REQ_LIST_TITLE;
    public static String GROW_NOT_ENOUGH_PLACE;
    public static String TREE_LIMIT;
    public static String DESTROY_SAPLING;
    public static String DESTROY_LEAVES_SANTA;
    public static String DESTROY_LEAVES_TUT;
    public static String DESTROY_WARNING;
    public static String DESTROY_FAIL_OWNER;
    public static String DESTROY_TUT;
    public static String CRYSTAL_NAME;
    public static List<String> CRYSTAL_LORE;
    public static String GIFT_LUCK;
    public static String GIFT_FAIL;
    public static String MONSTER;
    public static String TIMEOUT;
    public static String HAPPY_NEW_YEAR;
    private static FileConfiguration locale;
    private static final FileConfiguration def_locale = ConfigUtils.loadConfig(new File(Main.getInstance().getDataFolder() + "/locales/default.yml"));

    public static void loadLocale(String lang) {
        File file = new File(Main.getInstance().getDataFolder() + "/locales/" + lang + ".yml");
        if (!file.exists()) {
            TextUtils.sendConsoleMessage("Can't load locale '" + lang + "'");
            TextUtils.sendConsoleMessage("Switching to default locale 'en'");
            locale = def_locale;
        } else {
            locale = ConfigUtils.loadConfig(file);
            TextUtils.sendConsoleMessage("Locale '" + lang + "' successfuly loaded");
        }
        loadStrings();
    }

    private static void loadStrings() {
        PLUGIN_NAME = getString("plugin-name");
        PLUGIN_ENABLED = getString("messages.plugin-enabled");
        GROW_LVL_PROGRESS = getString("messages.tree.grow-lvl-progress");
        GROW_LVL_READY = getString("messages.tree.grow-lvl-ready");
        GROW_LEVEL_MAX = getString("messages.tree.grow-lvl-max");
        GROW_REQ_LIST_TITLE = getString("messages.tree.grow-req-list-title");
        GROW_NOT_ENOUGH_PLACE = getString("messages.tree.grow-not-enough-place");
        TREE_LIMIT = getString("messages.tree.tree-limit");
        DESTROY_SAPLING = getString("messages.tree.destroy-sapling");
        DESTROY_LEAVES_SANTA = getString("messages.tree.destroy-leaves-santa");
        DESTROY_LEAVES_TUT = getString("messages.tree.destroy-leaves-tut");
        MONSTER = getString("messages.tree.destroy-complete");
        DESTROY_WARNING = getString("messages.tree.destroy-warning");
        DESTROY_TUT = getString("messages.tree.destroy-tut");
        DESTROY_FAIL_OWNER = getString("messages.tree.destroy-fail-owner");
        CRYSTAL_NAME = getString("crystal.name");
        CRYSTAL_LORE = getStringList("crystal.lore");
        GIFT_LUCK = getString("messages.gift.luck-message");
        GIFT_FAIL = getString("messages.gift.unluck-message");
        TIMEOUT = getString("messages.timeout");
        HAPPY_NEW_YEAR = getString("messages.final-wish");

    }

    private static String getString(String path) {
        if (locale == null)
            throw new NullPointerException("Locale not loaded");

        try {
            String message = ChatColor.translateAlternateColorCodes('&', locale.getString(path));
            return message.contains("_UNUSED") ? null : message;
        } catch (NullPointerException e) {
            TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "Unable to find '" + path + "' in locale " + Main.getInstance().getConfig().getString("core.locale") + ". Bad File?");
            TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "Using default locale to get value");
            return def_locale.getString(path);
        }
    }

    private static List<String> getStringList(String path) {
        if (locale == null)
            throw new NullPointerException("Locale not loaded");

        try {
            return locale.getStringList(path);
        } catch (IllegalArgumentException e) {
            TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "Unable to find '" + path + "' in locale " + Main.getInstance().getConfig().getString("core.locale") + ". Bad File?");
            TextUtils.sendConsoleMessage(ChatColor.DARK_RED + "Using default locale to get value");
            return def_locale.getStringList(path);
        }

    }
}
