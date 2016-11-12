package ru.meloncode.xmas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

class MagicTask extends BukkitRunnable {

    @Override
    public void run() {
        for (MagicTree tree : XMas.getAllTrees()) {
            if (Main.EVENT_IN_PROGRESS)
                tree.update();
            if (Main.AUTO_END && Main.END_TIME < System.currentTimeMillis()) {
                Main.EVENT_IN_PROGRESS = false;
                cancel();
                Bukkit.broadcastMessage(ChatColor.GREEN + LocaleManager.HAPPY_NEW_YEAR);
            }
        }
    }
}
