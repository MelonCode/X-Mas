package ru.meloncode.xmas.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.meloncode.xmas.LocaleManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public class ConfigUtils {

    public static YamlConfiguration loadConfig(File file) {
        YamlConfiguration configuration = newConfiguration();
        if (!file.exists()) {
            return configuration;
        }

        try {
            configuration.loadFromString(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException exception) {
            Bukkit.getLogger().log(Level.WARNING, LocaleManager.text("console.config.load-yaml-failed", "Failed to load YAML configuration from {file}",
                    "{file}", file.getPath()), exception);
        }
        return configuration;
    }

    public static YamlConfiguration loadManagedConfig(JavaPlugin plugin, String resourcePath, File file) {
        YamlConfiguration configuration = loadConfig(file);
        boolean changed = synchronizeWithResource(plugin, resourcePath, configuration);
        if (!file.exists() || changed) {
            saveConfig(file, configuration);
        }
        return configuration;
    }

    public static boolean synchronizeWithResource(JavaPlugin plugin, String resourcePath, FileConfiguration configuration) {
        if (!(configuration instanceof YamlConfiguration yamlConfiguration)) {
            return false;
        }
        YamlConfiguration defaults = loadResourceConfig(plugin, resourcePath);
        return synchronizeWithDefaults(yamlConfiguration, defaults);
    }

    public static boolean synchronizeWithDefaults(FileConfiguration configuration, FileConfiguration defaults) {
        if (!(configuration instanceof YamlConfiguration yamlConfiguration) || !(defaults instanceof YamlConfiguration defaultYaml)) {
            return false;
        }
        return mergeDefaultsAndComments(yamlConfiguration, defaultYaml);
    }

    public static void saveConfig(File file, FileConfiguration configuration) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Bukkit.getLogger().warning(LocaleManager.text("console.config.create-directory-failed", "Unable to create configuration directory {directory}",
                    "{directory}", parent.getPath()));
            return;
        }

        try {
            configuration.save(file);
        } catch (IOException exception) {
            Bukkit.getLogger().log(Level.WARNING, LocaleManager.text("console.config.save-yaml-failed", "Failed to save YAML configuration to {file}",
                    "{file}", file.getPath()), exception);
        }
    }

    public static YamlConfiguration loadResourceConfig(JavaPlugin plugin, String resourcePath) {
        YamlConfiguration configuration = newConfiguration();
        try (InputStream inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) {
                plugin.getLogger().warning(LocaleManager.text("console.config.missing-bundled-resource", "Missing bundled configuration resource: {resource}",
                        "{resource}", resourcePath));
                return configuration;
            }
            configuration.loadFromString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().log(Level.WARNING, LocaleManager.text("console.config.load-bundled-resource-failed", "Failed to load bundled configuration resource {resource}",
                    "{resource}", resourcePath), exception);
        }
        return configuration;
    }

    private static YamlConfiguration newConfiguration() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        return configuration;
    }

    private static boolean mergeDefaultsAndComments(YamlConfiguration target, YamlConfiguration defaults) {
        boolean changed = false;

        if (isBlank(target.options().getHeader()) && !isBlank(defaults.options().getHeader())) {
            target.options().setHeader(defaults.options().getHeader());
            changed = true;
        }
        if (isBlank(target.options().getFooter()) && !isBlank(defaults.options().getFooter())) {
            target.options().setFooter(defaults.options().getFooter());
            changed = true;
        }

        for (String path : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(path)) {
                if (!target.isConfigurationSection(path)) {
                    target.createSection(path);
                    changed = true;
                }
            } else if (!target.contains(path, true)) {
                target.set(path, defaults.get(path));
                changed = true;
            }

            if (copyMissingComments(target, path, defaults.getComments(path), defaults.getInlineComments(path))) {
                changed = true;
            }
        }

        return changed;
    }

    private static boolean copyMissingComments(YamlConfiguration target, String path, List<String> blockComments, List<String> inlineComments) {
        boolean changed = false;
        if (isBlank(target.getComments(path)) && !isBlank(blockComments)) {
            target.setComments(path, blockComments);
            changed = true;
        }
        if (isBlank(target.getInlineComments(path)) && !isBlank(inlineComments)) {
            target.setInlineComments(path, inlineComments);
            changed = true;
        }
        return changed;
    }

    private static boolean isBlank(List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            return true;
        }
        for (String line : comments) {
            if (line != null && !line.isBlank()) {
                return false;
            }
        }
        return true;
    }

}
