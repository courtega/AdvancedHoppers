package net.earthmc.hopperfilter.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class InventoryMoveItemListener implements Listener {

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory initiator = event.getInitiator();
        if (!initiator.getType().equals(InventoryType.HOPPER)) return;

        if (!(initiator.getHolder() instanceof Hopper hopper)) return;

        String hopperName = serialiseComponent(hopper.customName());
        if (hopperName == null) return;

        if (!canItemPassFilter(hopperName, event.getItem())) event.setCancelled(true);
    }

    private boolean canItemPassFilter(String containerName, ItemStack item) {
        String[] split = containerName.split(",");
        String itemName = item.getType().toString().toLowerCase();

        for (String string : split) {
            String pattern = string.toLowerCase();
            int length = pattern.length();

            if (pattern.startsWith("*") && pattern.endsWith("*")) { // Contains specified pattern
                if (itemName.contains(pattern.substring(1, length - 1))) return true;
            } else if (pattern.startsWith("*")) { // Starts with specified pattern
                if (itemName.startsWith(pattern.substring(1))) return true;
            } else if (pattern.endsWith("*")) { // Ends with specified pattern
                if (itemName.endsWith(pattern.substring(0, length - 1))) return true;
            }

            if (pattern.equals(itemName)) return true;
        }

        return false;
    }

    private @Nullable String serialiseComponent(Component component) {
        if (component == null) return null;
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
