package ru.meloncode.xmas.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtils {

    public static FileConfiguration loadConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

}
