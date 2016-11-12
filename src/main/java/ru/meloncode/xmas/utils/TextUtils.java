package ru.meloncode.xmas.utils;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.meloncode.xmas.LocaleManager;
import ru.meloncode.xmas.MagicTree;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    public static List<String> generateChatReqList(MagicTree tree) {
        if (tree == null)
            throw new NullArgumentException("tree");
        List<String> list = new ArrayList<>();
        list.add(ChatColor.GOLD + LocaleManager.GROW_REQ_LIST_TITLE + ":");
        if (tree.getLevel().getLevelupRequirements() != null && tree.getLevel().getLevelupRequirements().size() > 0)
            for (Material cMaterial : tree.getLevel().getLevelupRequirements().keySet()) {
                int levelReq = tree.getLevel().getLevelupRequirements().get(cMaterial);
                int treeReq = 0;
                if (tree.getLevelupRequirements().containsKey(cMaterial))
                    treeReq = tree.getLevelupRequirements().get(cMaterial);

                list.add(ChatColor.BOLD + "" + (treeReq == 0 ? ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH : ChatColor.RED + "" + ChatColor.UNDERLINE) + WordUtils.capitalizeFully(String.valueOf(cMaterial) + " : " + (levelReq - treeReq + " / " + levelReq)));
            }
        return list;
    }

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null)
            player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + LocaleManager.PLUGIN_NAME + ChatColor.DARK_RED + "] " + ChatColor.RESET + message);
    }

    public static void sendConsoleMessage(String message) {
        if (message != null)
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + "X" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GREEN + "MAS" + ChatColor.DARK_RED + "] " + ChatColor.DARK_GREEN + message);
    }
}
