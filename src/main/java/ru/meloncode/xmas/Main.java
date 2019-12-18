package ru.meloncode.xmas;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import ru.meloncode.xmas.utils.TextUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends JavaPlugin implements Listener {

    // Yeah. That's as it should be.
    static final Random RANDOM = new Random(Calendar.getInstance().get(Calendar.YEAR));
    static List<ItemStack> gifts;
    static float LUCK_CHANCE;
    static boolean LUCK_CHANCE_ENABLED;
    static boolean resourceBack;
    static int MAX_TREE_COUNT;
    static boolean autoEnd;
    static long endTime;
    static boolean inProgress;
    private static int UPDATE_SPEED;
    private static int TASK_DELAY;
    private static int PARTICLES_DELAY;
    private static List<String> heads;
    private static Plugin plugin;
    private FileConfiguration config;
    private String locale;

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
        this.saveDefaults();
        config = getConfig();
        locale = config.getString("core.locale");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk-mm-ss");
        inProgress = config.getBoolean("core.plugin-enabled", true);
        UPDATE_SPEED = config.getInt("core.update-speed");
        if (UPDATE_SPEED <= 0) {
            TextUtils.sendConsoleMessage("Update speed must be > 0");
            TextUtils.sendConsoleMessage("Setting value to default");
            config.set("core.update-speed", 7);
            UPDATE_SPEED = 7;
        }
        TASK_DELAY = config.getInt("core.task-delay");
        if (TASK_DELAY <= 0)
            config.set("core.task-delay", 7);
        PARTICLES_DELAY = config.getInt("core.particles-delay");
        if (PARTICLES_DELAY <= 0)
            config.set("particles-delay", 35);
        
        autoEnd = config.getBoolean("core.holiday-ends.enabled");
        resourceBack = config.getBoolean("core.holiday-ends.resource-back");
        MAX_TREE_COUNT = config.getInt("core.tree-limit");
        Date date;
        try {
            date = sdf.parse(config.getString("core.holiday-ends.date"));
            endTime = date.getTime();
        } catch (ParseException e1) {
            TextUtils.sendConsoleMessage("Unable to load date");
        }
        defineTreeLevels();
        for (World world : getServer().getWorlds()) {
            TreeSerializer.loadTrees(this, world);
        }

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
                    material = Material.valueOf(split[0]);
                    if (split.length > 1) amount = Integer.parseInt(split[1]);
                    gifts.add(new ItemStack(material, amount));
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

        LUCK_CHANCE_ENABLED = config.getBoolean("xmas.luck.enabled");
        LUCK_CHANCE = (float) config.getInt("xmas.luck.chance") / 100;
        new Events().registerListener();
        new MagicTask(this).runTaskTimer(this, 5, TASK_DELAY);
        new MagicTaskParticles(this).runTaskTimer(this, 5, PARTICLES_DELAY);
        XMas.XMAS_CRYSTAL = new ItemMaker(Material.EMERALD, LocaleManager.CRYSTAL_NAME, LocaleManager.CRYSTAL_LORE).make();

        ShapedRecipe grinderRecipe;
        grinderRecipe = new ShapedRecipe(new NamespacedKey(this, "xmas"), XMas.XMAS_CRYSTAL).shape("#d#", "ded", "#d#").setIngredient('d', Material.DIAMOND).setIngredient('e', Material.EMERALD);
        Iterator<Recipe> recipes = getServer().recipeIterator();
        boolean registered = false;
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (recipe.equals(grinderRecipe)) {
                registered = true;
                break;
            }

        }
        try {
            if (!registered)
                getServer().addRecipe(grinderRecipe);
        } catch (Exception e) {
        }
        XMasCommand.register(this);
        TextUtils.sendConsoleMessage(LocaleManager.PLUGIN_ENABLED);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        TreeSerializer.loadTrees(this, event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (MagicTree magicTree : XMas.getAllTrees()) {
            if (magicTree.getLocation().getWorld() == event.getWorld()) magicTree.unbuild();
        }
    }


    @Override
    public void onDisable() {
        if (XMas.getAllTrees().size() > 0)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.unbuild();
            }
    }

    public void end() {
        Bukkit.broadcastMessage(ChatColor.GREEN + LocaleManager.HAPPY_NEW_YEAR);
        inProgress = false;
        config.set("core.plugin-enabled", false);
        saveConfig();
    }

    private void saveDefaults() {
        this.saveDefaultConfig();
        plugin.saveResource("locales/default.yml", true);
        ArrayList<String> defaults = Lists.newArrayList("locales/en.yml", "locales/ru.yml", "locales/ru_santa.yml", "trees.yml");
        for (String path : defaults)
            if (!new File(getDataFolder(), '/' + path).exists()) plugin.saveResource(path, false);
    }

    private void defineTreeLevels() {

        long sapling_delay = config.getInt("xmas.tree-lvl.sapling.gift-cooldown") * 20 / UPDATE_SPEED;
        long small_delay = config.getInt("xmas.tree-lvl.small_tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long tree_delay = config.getInt("xmas.tree-lvl.tree.gift-cooldown") * 20 / UPDATE_SPEED;
        long magic_delay = config.getInt("xmas.tree-lvl.magic_tree.gift-cooldown") * 20 / UPDATE_SPEED;

        ConfigurationSection lvlups = config.getConfigurationSection("xmas.tree-lvl");
        Map<Material, Integer> saplingLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("sapling.lvlup").getValues(false));
        Map<Material, Integer> smallLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("small_tree.lvlup").getValues(false));
        Map<Material, Integer> treeLevelUp = TreeSerializer.convertRequirementsMap(lvlups.getConfigurationSection("tree.lvlup").getValues(false));

        TreeLevel.MAGIC_TREE = new TreeLevel("magic_tree", Effects.TREE_WHITE_AMBIENT, Effects.TREE_SWAG, null, null, magic_delay, Collections.emptyMap(), new StructureTemplate(new HashMap<Vector, Material>() {
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

        TreeLevel.TREE = new TreeLevel("tree", Effects.AMBIENT_SNOW, Effects.TREE_GOLD_SWAG, null, TreeLevel.MAGIC_TREE, tree_delay, treeLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
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

        TreeLevel.SMALL_TREE = new TreeLevel("small_tree", Effects.AMBIENT_PORTAL, Effects.TREE_RED_SWAG, null, TreeLevel.TREE, small_delay, smallLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
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

        TreeLevel.SAPLING = new TreeLevel("sapling", Effects.AMBIENT_SAPLING, null, null, TreeLevel.SMALL_TREE, sapling_delay, saplingLevelUp, new StructureTemplate(new HashMap<Vector, Material>() {
            private static final long serialVersionUID = 1L;

            {
                put(new Vector(0, -1, 0), Material.GRASS_BLOCK);
                put(new Vector(0, 0, 0), Material.SPRUCE_SAPLING);
            }
        }));
    }
}
