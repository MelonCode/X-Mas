package ru.meloncode.xmas;

import org.bukkit.scheduler.BukkitRunnable;

public class MagicTask extends BukkitRunnable {

	@Override
	public void run() {
		for (MagicTree tree : XMas.getAllTrees()) {
			if (Main.EVENT_IN_PROGRESS)
				tree.update();
			if (Main.AUTO_END && Main.END_TIME < System.currentTimeMillis()) {
				Main.EVENT_IN_PROGRESS = false;
			}
		}
	}
}
