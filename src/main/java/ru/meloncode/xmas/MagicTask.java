package ru.meloncode.xmas;

import org.bukkit.scheduler.BukkitRunnable;

class MagicTask extends BukkitRunnable {

    private final Main xmas;

    MagicTask(Main main) {
        this.xmas = main;
    }

    @Override
    public void run() {
        if (Main.inProgress)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.update();
            }
        if ((Main.autoEnd && Main.endTime < System.currentTimeMillis()) || !Main.inProgress) {
            xmas.end();
            cancel();
        }
    }
}
