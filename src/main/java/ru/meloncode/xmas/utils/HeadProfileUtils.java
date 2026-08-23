package ru.meloncode.xmas.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.profile.PlayerTextures;
import ru.meloncode.xmas.LocaleManager;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

public final class HeadProfileUtils {
    public enum HeadConfigType {
        TEXTURE_URL,
        PLAYER_NAME,
        INVALID
    }

    public record HeadConfigValidation(HeadConfigType type, String warning) {
        public boolean valid() {
            return type != HeadConfigType.INVALID;
        }
    }

    private HeadProfileUtils() {
    }

    public static HeadConfigValidation validateConfiguredHead(String configuredHead) {
        if (configuredHead == null || configuredHead.trim().isEmpty()) {
            return new HeadConfigValidation(
                    HeadConfigType.INVALID,
                    LocaleManager.text("console.heads.blank", "Blank present head entry.")
            );
        }

        String trimmedHead = configuredHead.trim();
        if (!trimmedHead.contains("://")) {
            return new HeadConfigValidation(HeadConfigType.PLAYER_NAME, null);
        }

        try {
            URL skinUrl = URI.create(trimmedHead).toURL();
            if (!"textures.minecraft.net".equalsIgnoreCase(skinUrl.getHost())) {
                return new HeadConfigValidation(
                        HeadConfigType.INVALID,
                        LocaleManager.text("console.heads.non-mojang-url", "Ignoring non-Mojang present skin URL: {url}",
                                "{url}", trimmedHead)
                );
            }
            return new HeadConfigValidation(HeadConfigType.TEXTURE_URL, null);
        } catch (IllegalArgumentException | MalformedURLException exception) {
            return new HeadConfigValidation(
                    HeadConfigType.INVALID,
                    LocaleManager.text("console.heads.invalid-url", "Invalid present skin URL: {url}",
                            "{url}", trimmedHead)
            );
        }
    }

    public static void applyConfiguredHead(Skull skull, String configuredHead, Logger logger) {
        Object legacyProfile = createLegacyProfile(configuredHead, logger);
        if (legacyProfile == null) {
            return;
        }

        if (trySetResolvableProfile(skull, legacyProfile)) {
            return;
        }
        if (trySetLegacyProfile(skull, legacyProfile)) {
            return;
        }

        logger.warning(LocaleManager.text("console.heads.apply-failed", "Unable to apply configured present head profile."));
    }

    private static Object createLegacyProfile(String configuredHead, Logger logger) {
        HeadConfigValidation validation = validateConfiguredHead(configuredHead);
        if (!validation.valid()) {
            if (validation.warning() != null) {
                logger.warning(validation.warning());
            }
            return null;
        }

        String trimmedHead = configuredHead.trim();
        try {
            if (!trimmedHead.contains("://")) {
                Method createProfile = Bukkit.class.getMethod("createProfile", String.class);
                return createProfile.invoke(null, trimmedHead);
            }

            URL skinUrl = URI.create(trimmedHead).toURL();
            Method createProfile = Bukkit.class.getMethod("createProfile", UUID.class);
            Object legacyProfile = createProfile.invoke(null, UUID.randomUUID());
            Method getTextures = legacyProfile.getClass().getMethod("getTextures");
            PlayerTextures textures = (PlayerTextures) getTextures.invoke(legacyProfile);
            textures.setSkin(skinUrl);
            Method setTextures = legacyProfile.getClass().getMethod("setTextures", PlayerTextures.class);
            setTextures.invoke(legacyProfile, textures);
            return legacyProfile;
        } catch (IllegalArgumentException | MalformedURLException exception) {
            logger.warning(LocaleManager.text("console.heads.invalid-url", "Invalid present skin URL: {url}",
                    "{url}", trimmedHead));
            return null;
        } catch (ReflectiveOperationException exception) {
            logger.warning(LocaleManager.text("console.heads.build-failed", "Failed to build present head profile: {error}",
                    "{error}", exception.getMessage()));
            return null;
        }
    }

    private static boolean trySetResolvableProfile(Skull skull, Object legacyProfile) {
        try {
            Class<?> legacyProfileClass = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            Class<?> resolvableProfileClass = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
            Method factoryMethod = resolvableProfileClass.getMethod("resolvableProfile", legacyProfileClass);
            Object resolvableProfile = factoryMethod.invoke(null, legacyProfile);
            Method setProfile = skull.getClass().getMethod("setProfile", resolvableProfileClass);
            setProfile.invoke(skull, resolvableProfile);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private static boolean trySetLegacyProfile(Skull skull, Object legacyProfile) {
        try {
            Class<?> legacyProfileClass = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            Method setPlayerProfile = skull.getClass().getMethod("setPlayerProfile", legacyProfileClass);
            setPlayerProfile.invoke(skull, legacyProfile);
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }
}
