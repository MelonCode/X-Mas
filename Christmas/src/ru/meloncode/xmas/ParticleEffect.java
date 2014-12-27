package ru.meloncode.xmas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

public class ParticleEffect {

	EffectType type;
	float offsetX, offsetY, offsetZ, speed;
	int count;

	public ParticleEffect(EffectType type, float offsetX, float offsetY, float offsetZ, float speed, int count) {
		this.type = type;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.count = count;
	}

	public void playEffect(Location location) {
		location = location.clone(); // prevent chaning pos of object
		location.add(0.5, 0.5, 0.5); // A small fix
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
		try {
			ReflectionUtilities.setValue(packet, "a", type.getParticleName());
			ReflectionUtilities.setValue(packet, "b", (float) location.getX());
			ReflectionUtilities.setValue(packet, "c", (float) location.getY());
			ReflectionUtilities.setValue(packet, "d", (float) location.getZ());
			ReflectionUtilities.setValue(packet, "e", offsetX);
			ReflectionUtilities.setValue(packet, "f", offsetY);
			ReflectionUtilities.setValue(packet, "g", offsetZ);
			ReflectionUtilities.setValue(packet, "h", speed);
			ReflectionUtilities.setValue(packet, "i", count);
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getLocation().getWorld() == location.getWorld())
					if (player.getLocation().distance(location) <= 16) {
						((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
					}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	enum EffectType {

		HUGE_EXPLOSION("hugeexplosion"),
		LARGE_EXPLODE("largeexplode"),
		FIREWORKS_SPARK("fireworksSpark"),
		BUBBLE("bubble"),
		SUSPEND("suspend"),
		DEPTH_SUSPEND("depthSuspend"),
		TOWN_AURA("townaura"),
		CRIT("crit"),
		MAGIC_CRIT("magicCrit"),
		MOB_SPELL("mobSpell"),
		MOB_SPELL_AMBIENT("mobSpellAmbient"),
		SPELL("spell"),
		INSTANT_SPELL("instantSpell"),
		WITCH_MAGIC("witchMagic"),
		NOTE("note"),
		PORTAL("portal"),
		ENCHANTMENT_TABLE("enchantmenttable"),
		EXPLODE("explode"),
		FLAME("flame"),
		LAVA("lava"),
		FOOTSTEP("footstep"),
		SPLASH("splash"),
		LARGE_SMOKE("largesmoke"),
		CLOUD("cloud"),
		RED_DUST("reddust"),
		SNOWBALL_POOF("snowballpoof"),
		DRIP_WATER("dripWater"),
		DRIP_LAVA("dripLava"),
		SNOW_SHOVEL("snowshovel"),
		SLIME("slime"),
		HEART("heart"),
		ANGRY_VILLAGER("angryVillager"),
		HAPPY_VILLAGER("happyVillager"),
		ICONCRACK("iconcrack_"),
		TILECRACK("tilecrack_");

		private String particleName;

		EffectType(String particleName) {
			this.particleName = particleName;
		}

		public String getParticleName() {
			return particleName;
		}

	}
}
