package ru.meloncode.xmas.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.meloncode.xmas.LocaleManager;
import ru.meloncode.xmas.MagicTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TextUtils {
    public static final String DISPLAY_NAME = "XMas Tree";
    private static final String FALLBACK_SUCCESS_HEX = "#b9e8b5";
    private static final String FALLBACK_ERROR_HEX = "#f3a7a7";

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

    public static List<Component> generateChatReqList(MagicTree tree) {
        Objects.requireNonNull(tree, "tree");
        List<Component> list = new ArrayList<>();
        Component title = parse("<xm-label><bold>" + escape(LocaleManager.GROW_REQ_LIST_TITLE) + "</bold></xm-label>");
        if (LocaleManager.GROW_REQ_LIST_HINT != null && !LocaleManager.GROW_REQ_LIST_HINT.isBlank()) {
            title = title.hoverEvent(HoverEvent.showText(parse(LocaleManager.GROW_REQ_LIST_HINT)));
        }
        list.add(title);
        if (tree.getLevel().getLevelupRequirements() != null && tree.getLevel().getLevelupRequirements().size() > 0)
            for (Material cMaterial : tree.getLevel().getLevelupRequirements().keySet()) {
                int levelReq = tree.getLevel().getLevelupRequirements().get(cMaterial);
                int treeReq = 0;
                if (tree.getLevelupRequirements().containsKey(cMaterial))
                    treeReq = tree.getLevelupRequirements().get(cMaterial);

                TextColor color = treeReq == 0
                        ? themeColor("xm-success", FALLBACK_SUCCESS_HEX)
                        : themeColor("xm-error", FALLBACK_ERROR_HEX);
                Component line = Component.translatable(cMaterial.getItemTranslationKey())
                        .color(color)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(" : " + (levelReq - treeReq) + " / " + levelReq, color).decorate(TextDecoration.BOLD));
                if (treeReq == 0) {
                    line = line.decorate(TextDecoration.STRIKETHROUGH);
                }
                list.add(line);
            }
        return list;
    }

    public static void sendMessage(Player player, String message) {
        sendMessage((CommandSender) player, message);
    }

    public static void sendMessage(Player player, Component message) {
        sendMessage((CommandSender) player, message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(parse(prefix() + message));
        }
    }

    public static void sendMessage(CommandSender sender, Component message) {
        if (sender != null && message != null) {
            sender.sendMessage(parse(prefix()).append(message));
        }
    }

    public static void sendRawMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(parse(message));
        }
    }

    public static void sendConsoleMessage(String message) {
        if (message != null) {
            Bukkit.getConsoleSender().sendMessage(parse(consolePrefix() + message));
        }
    }

    public static Component parse(String message) {
        if (message == null) {
            return Component.empty();
        }
        String themed = applyThemeAliases(LocaleManager.replaceCommonTokens(message)).replace("\\>", ">");
        if (themed.indexOf('§') >= 0) {
            return LEGACY_SECTION.deserialize(themed);
        }
        if (themed.indexOf('&') >= 0 && themed.indexOf('<') < 0) {
            return LEGACY_AMPERSAND.deserialize(themed);
        }
        return MINI_MESSAGE.deserialize(themed);
    }

    public static List<Component> parseList(List<String> messages) {
        List<Component> components = new ArrayList<>();
        if (messages != null) {
            for (String message : messages) {
                components.add(parse(message));
            }
        }
        return components;
    }

    public static String displayName() {
        if (LocaleManager.PLUGIN_NAME != null && !LocaleManager.PLUGIN_NAME.isBlank()) {
            return LocaleManager.PLUGIN_NAME;
        }
        return DISPLAY_NAME;
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return MINI_MESSAGE.escapeTags(text);
    }

    public static String title(String text) {
        return "<xm-accent><bold>" + escape(text) + "</bold></xm-accent>";
    }

    public static String label(String text) {
        return "<xm-label>" + escape(text) + "</xm-label>";
    }

    public static String text(String text) {
        return "<xm-text>" + escape(text) + "</xm-text>";
    }

    public static String muted(String text) {
        return "<xm-muted>" + escape(text) + "</xm-muted>";
    }

    public static String accent(String text) {
        return "<xm-accent>" + escape(text) + "</xm-accent>";
    }

    public static String accentSecondary(String text) {
        return "<xm-accent-2>" + escape(text) + "</xm-accent-2>";
    }

    public static String command(String text) {
        return "<xm-command>" + escape(text) + "</xm-command>";
    }

    public static String success(String text) {
        return "<xm-success>" + escape(text) + "</xm-success>";
    }

    public static String warning(String text) {
        return "<xm-warning>" + escape(text) + "</xm-warning>";
    }

    public static String error(String text) {
        return "<xm-error>" + escape(text) + "</xm-error>";
    }

    public static String info(String text) {
        return "<xm-info>" + escape(text) + "</xm-info>";
    }

    public static String booleanValue(boolean value) {
        return value ? success("true") : error("false");
    }

    private static String prefix() {
        return LocaleManager.getChatPrefix();
    }

    private static String consolePrefix() {
        return LocaleManager.getConsolePrefix();
    }

    private static String applyThemeAliases(String message) {
        String themed = message;
        for (Map.Entry<String, String> entry : LocaleManager.getThemeAliases().entrySet()) {
            themed = themed.replace(entry.getKey(), entry.getValue());
        }
        return themed;
    }

    private static TextColor themeColor(String themeAlias, String fallbackHex) {
        String alias = LocaleManager.getThemeAliases().get("<" + themeAlias + ">");
        if (alias != null && alias.startsWith("<#") && alias.endsWith(">")) {
            TextColor fromAlias = TextColor.fromHexString(alias.substring(1, alias.length() - 1));
            if (fromAlias != null) {
                return fromAlias;
            }
        }
        return TextColor.fromHexString(fallbackHex);
    }
}
