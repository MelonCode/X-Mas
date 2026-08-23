package ru.meloncode.xmas;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class XMasPlaceholderExpansion extends PlaceholderExpansion {
    private final Main plugin;

    public XMasPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return XMasPlaceholders.IDENTIFIER;
    }

    @Override
    public String getAuthor() {
        return "mrfloris";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public List<String> getPlaceholders() {
        return XMasPlaceholders.EXAMPLES;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return XMasPlaceholders.resolve(plugin, player, params);
    }
}
