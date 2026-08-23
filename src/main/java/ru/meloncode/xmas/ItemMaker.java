package ru.meloncode.xmas;

//I plan to make this plugin bigger. So... 

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.format.TextDecoration;
import ru.meloncode.xmas.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemMaker {

    private final ItemStack is;
    private ItemMeta im;

    public ItemMaker(Material material) {
        is = new ItemStack(material);
        im = is.getItemMeta();
    }

    public ItemMaker(Material material, String name) {
        is = new ItemStack(material);
        im = is.getItemMeta();
        im.displayName(TextUtils.parse(name).decoration(TextDecoration.ITALIC, false));
    }

    public ItemMaker(Material material, String name, List<String> lore) {
        is = new ItemStack(material);
        im = is.getItemMeta();
        im.displayName(TextUtils.parse(name).decoration(TextDecoration.ITALIC, false));
        im.lore(TextUtils.parseList(lore));
    }

    public ItemMaker(Material material, int amount, short durability) {
        is = new ItemStack(material, amount);
        im = is.getItemMeta();
    }

    public ItemMaker(Material material, int amount, short durability, String name) {
        is = new ItemStack(material, amount);
        im = is.getItemMeta();
        im.displayName(TextUtils.parse(name).decoration(TextDecoration.ITALIC, false));
    }

    public ItemMaker(Material material, int amount, short durability, String name, List<String> lore) {
        is = new ItemStack(material, amount);
        im = is.getItemMeta();
        im.displayName(TextUtils.parse(name).decoration(TextDecoration.ITALIC, false));
        im.lore(TextUtils.parseList(lore));
    }

    public ItemMaker setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    public ItemMaker setDurability(short data) {
        if (im instanceof Damageable damageable) {
            damageable.setDamage(data);
        }
        return this;
    }

    public ItemMaker setName(String name) {
        im.displayName(TextUtils.parse("<reset>" + name).decoration(TextDecoration.ITALIC, false));
        return this;
    }

    public ItemMaker setLore(List<String> lore) {
        im.lore(TextUtils.parseList(lore));
        return this;
    }

    public ItemMaker addLoreLine(String line) {
        List<Component> lore;
        if (im.lore() != null) {
            lore = new ArrayList<>(im.lore());
        } else {
            lore = new ArrayList<>();
        }
        lore.add(TextUtils.parse(line));
        im.lore(lore);

        return this;
    }

    public ItemMaker addEnchant(Enchantment enchantment) {
        im.addEnchant(enchantment, 1, true);
        return this;
    }

    public ItemMaker addEnchant(Enchantment enchantment, int level) {
        im.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemStack make() {
        is.setItemMeta(im);
        im = null;
        return is;
    }
}
