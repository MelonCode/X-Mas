package ru.meloncode.xmas;

import org.bukkit.scheduler.BukkitRunnable;

class MagicTaskParticles extends BukkitRunnable {

    private final Main xmas;

    MagicTaskParticles(Main main) {
        this.xmas = main;
    }

    @Override
    public void run() {
        if (Main.inProgress)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.playParticles();
            }
        if ((Main.autoEnd && Main.endTime < System.currentTimeMillis()) || !Main.inProgress) {
            xmas.end();
            cancel();
        }
    }
}
