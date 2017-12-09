package ru.meloncode.xmas.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.meloncode.xmas.Main;

import java.io.File;
import java.io.InputStream;

public class ConfigUtils {

    public static FileConfiguration loadConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

}
