package ru.meloncode.xmas;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends JavaPlugin {

    // Yeah. That's as it should be.
    static final Random RANDOM = new Random(Calendar.getInstance().get(Calendar.YEAR));
    static List<ItemStack> gifts;
    static float LUCK_CHANCE;
    static boolean LUCKCHANCEENABLED;
    static boolean BACK_RESOURCES;
    static int MAX_TREE_COUNT;
    static boolean AUTO_END;
    static long END_TIME;
    static boolean EVENT_IN_PROGRESS = true;
    private static int UPDATE_SPEED;
    private static List<String> heads;
    private static Plugin plugin;
    private final FileConfiguration config = getConfig();
    private final String locale = config.getString("core.locale");

    public static Plugin getInstance() {
        return plugin;
    }

    public static List<String> getHeads() {
        return heads;
    }

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveDefaultLocales();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");
        UPDATE_SPEED = config.getInt("core.update-speed");
        if (UPDATE_SPEED <= 0) {
            TextUtils.sendConsoleMessage("Update speed must be > 0");
            TextUtils.sendConsoleMessage("Setting value to default");
            config.set("core.update-speed", 7);
            UPDATE_SPEED = 7;
        }
        AUTO_END = config.getBoolean("core.holiday-ends.enabled");
        BACK_RESOURCES = config.getBoolean("core.holiday-ends.resource-back");
        MAX_TREE_COUNT = config.getInt("core.tree-limit");
        Date date;
        try {
            date = sdf.parse(config.getString("core.holiday-ends.date"));
            END_TIME = date.getTime();
        } catch (ParseException e1) {
            TextUtils.sendConsoleMessage("Unable to load date");
        }
        defineTreeLevels();
        TreeSerializer.loadTrees();
        LocaleManager.loadLocale(locale);
        heads = config.getStringList("xmas.presents");
        if (heads.size() == 0) {
            getLogger().warning(ChatColor.RED + "Warning! No heads loaded! Presents can't spawn without box!");
            return;
        }
        gifts = new ArrayList<>();
        for (String cItem : config.getStringList("xmas.gifts")) {
            try {

                if (cItem.contains(":")) {
                    String[] split = cItem.split(":");
                    if (split.length == 0) throw new IllegalArgumentException();

                    Material material;
                    int amount = 1;
                    short data = 0;

                    material = Material.valueOf(split[0]);
                    if (split.length > 1) amount = Integer.parseInt(split[1]);
                    if (split.length > 2) data = Short.parseShort(split[2]);
                    gifts.add(new ItemStack(material, amount, data));
                } else {
                    gifts.add(new ItemStack(Material.valueOf(cItem)));
                }

            } catch (IllegalArgumentException e) {
                getLogger().severe(ChatColor.RED + "[X-Mas] Unable to get load gift from  '" + cItem + "'");
                getLogger().warning(ChatColor.RED + "[X-Mas] For gifts - use format MATERIAL:AMOUNT:DATA. Amount and data are optional");
            }
        }
        if (gifts.size() == 0) {
            getLogger().warning(ChatColor.RED + "[X-Mas] Warning! No gifts loaded! No X-Mas without gifts!");
            return;
        }

        LUCKCHANCEENABLED = config.getBoolean("xmas.luck.enabled");
        LUCK_CHANCE = (float) config.getInt("xmas.luck.chance") / 100;
        new Events().registerListener();
        new MagicTask().runTaskTimer(this, 5, UPDATE_SPEED);
        XMas.XMAS_CRYSTAL = new ItemMaker(Material.EMERALD, LocaleManager.CRYSTAL_NAME, LocaleManager.CRYSTAL_LORE).make();

        ShapedRecipe grinderRecipe = new ShapedRecipe(XMas.XMAS_CRYSTAL).shape("#d#", "ded", "#d#").setIngredient('d', Material.DIAMOND).setIngredient('e', Material.EMERALD);
        getServer().addRecipe(grinderRecipe);
        TextUtils.sendConsoleMessage(LocaleManager.PLUGIN_ENABLED);
    }

    @Override
    public void onDisable() {
        if (XMas.getAllTrees().size() > 0)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.unbuild();
            }
    }

    private void saveDefaultLocales() {
        // Why does my cooode.. Feeling so bad..
        plugin.saveResource("locales/" + "default.yml", true);
        if (!new File(getDataFolder(), "/locales/en.yml").exists()) {
            plugin.saveResource("locales/" + "en.yml", false);
        }
        if (!new File(getDataFolder(), "/locales/ru.yml").exists()) {
            plugin.saveResource("locales/" + "ru.yml", false);
        }
        if (!new File(getDataFolder(), "/locales/ru_santa.yml").exists()) {
            plugin.saveResource("locales/" + "ru_santa.yml", false);
        }
        if (!new File(getDataFolder(), "trees.yml").exists()) {
            plugin.saveResource("trees.yml", false);
        }
    }

    private void defineTreeLevels() {

        long sapling_delay = config.getInt("xmas.tree-lvl.sapling.gift-cooldown") * 20 / UPDATE_SPEED;
        long small_delay = config.getInt("xmas.tree-lvl.small_tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long tree_delay = config.getInt("xmas.tree-lvl.tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long magic_delay = config.getInt("xmas.tree-lvl.magic_tree.gift-cooldown") * 20 / UPDATE_SPEED;

        Map<Material, Integer> sapling_levelup = new HashMap<>();
        Map<Material, Integer> small_levelup = new HashMap<>();
        Map<Material, Integer> tree_levelup = new HashMap<>();
        Map<Material, Integer> magic_levelup = new HashMap<>();

        if (config.getConfigurationSection("xmas.tree-lvl.sapling.lvlup") != null)
            sapling_levelup = TreeSerializer.convertRequirementsMap(config.getConfigurationSection("xmas.tree-lvl.sapling.lvlup").getValues(false));
        if (config.getConfigurationSection("xmas.tree-lvl.small_tree.lvlup") != null)
            small_levelup = TreeSerializer.convertRequirementsMap(config.getConfigurationSection("xmas.tree-lvl.small_tree.lvlup").getValues(false));
        if (config.getConfigurationSection("xmas.tree-lvl.tree.lvlup") != null)
            tree_levelup = TreeSerializer.convertRequirementsMap(config.getConfigurationSection("xmas.tree-lvl.tree.lvlup").getValues(false));
        if (config.getConfigurationSection("xmas.tree-lvl.magic_tree.lvlup") != null)
            magic_levelup = TreeSerializer.convertRequirementsMap(config.getConfigurationSection("xmas.tree-lvl.magic_tree.lvlup").getValues(false));

        TreeLevel.MAGIC_TREE = new TreeLevel("magic_tree", Effects.TREE_WHITE_AMBIENT, Effects.TREE_SWAG, null, null, magic_delay, magic_levelup, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS);
                for (int i = 0; i <= 5; i++) {
                    put(new Vector(0, i, 0), Material.LOG);
                    if (i >= 2) {
                        put(new Vector(1, i, 0), Material.LEAVES);
                        put(new Vector(-1, i, 0), Material.LEAVES);
                        put(new Vector(0, i, 1), Material.LEAVES);
                        put(new Vector(0, i, -1), Material.LEAVES);
                    }
                }
                put(new Vector(0, 6, 0), Material.LEAVES);

                put(new Vector(0, 7, 0), Material.GLOWSTONE);// Star

                put(new Vector(1, 4, 0), Material.LEAVES);
                put(new Vector(1, 4, 1), Material.LEAVES);
                put(new Vector(1, 4, -1), Material.LEAVES);
                put(new Vector(-1, 4, -1), Material.LEAVES);
                put(new Vector(-1, 4, 1), Material.LEAVES);

                put(new Vector(1, 2, 1), Material.LEAVES);
                put(new Vector(-1, 2, -1), Material.LEAVES);
                put(new Vector(1, 2, -1), Material.LEAVES);
                put(new Vector(-1, 2, 1), Material.LEAVES);

                put(new Vector(2, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, 2), Material.LEAVES);
                put(new Vector(-2, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, -2), Material.LEAVES);
            }
        }));

        TreeLevel.TREE = new TreeLevel("tree", Effects.AMBIENT_SNOW, Effects.TREE_GOLD_SWAG, null, TreeLevel.MAGIC_TREE, tree_delay, tree_levelup, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS);
                put(new Vector(0, 0, 0), Material.LOG);
                put(new Vector(0, 1, 0), Material.LOG);
                put(new Vector(0, 2, 0), Material.LOG);
                put(new Vector(0, 3, 0), Material.LOG);
                put(new Vector(0, 4, 0), Material.LOG);
                put(new Vector(0, 5, 0), Material.LEAVES);
                put(new Vector(1, 4, 0), Material.LEAVES);
                put(new Vector(0, 4, 1), Material.LEAVES);
                put(new Vector(-1, 4, 0), Material.LEAVES);
                put(new Vector(0, 4, -1), Material.LEAVES);

                put(new Vector(1, 1, 0), Material.LEAVES);
                put(new Vector(0, 1, 1), Material.LEAVES);
                put(new Vector(1, 1, 1), Material.LEAVES);
                put(new Vector(-1, 1, 0), Material.LEAVES);
                put(new Vector(0, 1, -1), Material.LEAVES);
                put(new Vector(-1, 1, -1), Material.LEAVES);
                put(new Vector(-1, 1, 1), Material.LEAVES);
                put(new Vector(1, 1, -1), Material.LEAVES);

                put(new Vector(1, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, 1), Material.LEAVES);
                put(new Vector(-1, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, -1), Material.LEAVES);

            }
        }));

        TreeLevel.SMALL_TREE = new TreeLevel("small_tree", Effects.AMBIENT_PORTAL, Effects.TREE_RED_SWAG, null, TreeLevel.TREE, small_delay, small_levelup, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS);
                put(new Vector(0, 0, 0), Material.LOG);
                put(new Vector(0, 1, 0), Material.LOG);
                put(new Vector(0, 2, 0), Material.LEAVES);
                put(new Vector(0, 3, 0), Material.LEAVES);

                put(new Vector(1, 1, 0), Material.LEAVES);
                put(new Vector(0, 1, 1), Material.LEAVES);
                put(new Vector(1, 1, 1), Material.LEAVES);
                put(new Vector(-1, 1, 0), Material.LEAVES);
                put(new Vector(0, 1, -1), Material.LEAVES);
                put(new Vector(-1, 1, -1), Material.LEAVES);
                put(new Vector(-1, 1, 1), Material.LEAVES);
                put(new Vector(1, 1, -1), Material.LEAVES);

                put(new Vector(1, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, 1), Material.LEAVES);
                put(new Vector(-1, 2, 0), Material.LEAVES);
                put(new Vector(0, 2, -1), Material.LEAVES);

            }
        }));

        TreeLevel.SAPLING = new TreeLevel("sapling", Effects.AMBIENT_SAPLING, null, null, TreeLevel.SMALL_TREE, sapling_delay, sapling_levelup, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS);
                put(new Vector(0, 0, 0), Material.SAPLING);
            }
        }));
    }
}
