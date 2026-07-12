package ru.meloncode.xmas;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

class JournalInventoryHolder implements InventoryHolder {
    private final UUID owner;

    JournalInventoryHolder(UUID owner) {
        this.owner = owner;
    }

    public UUID owner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
