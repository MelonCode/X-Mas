package ru.meloncode.xmas;

import org.bukkit.scheduler.BukkitRunnable;

class PlayParticlesTask extends BukkitRunnable {

    private final Main xmas;

    PlayParticlesTask(Main main) {
        this.xmas = main;
    }

    @Override
    public void run() {
        if (Main.inProgress)
            for (MagicTree tree : XMas.getAllTrees()) {
                tree.playParticles();
            }
    }
}
