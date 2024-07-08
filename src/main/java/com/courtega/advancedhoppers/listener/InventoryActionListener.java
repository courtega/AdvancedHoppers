package com.courtega.advancedhoppers.listener;

import com.courtega.advancedhoppers.FilteredHoppersPlugin;
import com.moandjiezana.toml.Toml;
import org.bukkit.*;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class InventoryActionListener implements Listener {

    //region Constants
    public final static String RESET_COMMAND = ":reset";
    public final static char NOT_OPERATOR = '!';
    public final static char OR_OPERATOR = '|';
    public final static char AND_OPERATOR = '&';
    public final static char CONTAINS_OPERATOR = '*';
    public final static char STARTS_OPERATOR = '^';
    public final static char ENDS_OPERATOR = '$';
    public final static char TAG_INDICATOR = '#';
    public final static char ENCHANT_INDICATOR = '+';
    public final static char ENCHANT_LEVEL_DELIMITER = ':';
    //endregion
    public final long DiscardedItemCacheEntryExpiry;

    private final BukkitScheduler Scheduler;
    private final FilteredHoppersPlugin Plugin;
    private final Map<Integer, Integer> ProhibitedItemsEx = new HashMap<>();

    public InventoryActionListener(FilteredHoppersPlugin plugin) {
        this.Plugin = plugin;
        Toml tomlConfig = plugin.getTomlConfig();
        this.Scheduler = plugin.getServer().getScheduler();

        this.DiscardedItemCacheEntryExpiry = tomlConfig.getTable("Technical").getLong("discarded_item_cache_entry_expiry");
    }

    public static Map<String, String> getConstants() {
        Map<String, String> constants = new HashMap<>();
        constants.put("reset", RESET_COMMAND);
        constants.put("not", String.valueOf(NOT_OPERATOR));
        constants.put("or", String.valueOf(OR_OPERATOR));
        constants.put("and", String.valueOf(AND_OPERATOR));
        constants.put("contains", String.valueOf(CONTAINS_OPERATOR));
        constants.put("starts", String.valueOf(STARTS_OPERATOR));
        constants.put("ends", String.valueOf(ENDS_OPERATOR));
        constants.put("tag", String.valueOf(TAG_INDICATOR));
        constants.put("enchant", String.valueOf(ENCHANT_INDICATOR));
        return constants;
    }

    //region Private Methods
    private static int getFurthestEmptySlot(ItemStack[] inventoryContents) {
        final int inventoryLength = inventoryContents.length;
        int slotPosition = 0;
        for (int i = inventoryLength; i != 0; i--) {
            ItemStack slotContents = inventoryContents[i - 1];
            if (slotContents == null) {
                slotPosition = i - 1;
                break;
            }
        }
        return slotPosition;
    }

    private static int getNearestOccupiedSlot(ItemStack[] inventoryContents) {
        final int inventoryLength = inventoryContents.length;
        int slotPosition = inventoryLength - 1;
        for (int i = 0; i < (inventoryLength - 1); i++) {
            ItemStack slotContents = inventoryContents[i];
            if (slotContents != null) {
                slotPosition = i;
                break;
            }
        }
        return slotPosition;
    }
    //endregion

    //region Filter Logic
    private static boolean isItemProhibited(final ItemStack itemStack, final String filterPattern) {
        boolean orSubOperandResultantBoolean = false;

        nextPatternGroup:
        for (final String orOperand : filterPattern.split(Pattern.quote(String.valueOf(OR_OPERATOR)))) {
            for (String andOperand : orOperand.split(Pattern.quote(String.valueOf(AND_OPERATOR)))) {
                andOperand = andOperand.toLowerCase().strip();
                final String itemName = itemStack.getType().getKey().getKey();

                final char prefix = andOperand.charAt(0);
                final String subOperand = andOperand.substring(1);

                boolean permitted = switch (prefix) {
                    default -> (andOperand.equals(itemName));

                    case NOT_OPERATOR -> isItemProhibited(itemStack, subOperand);
                    case CONTAINS_OPERATOR -> itemName.contains(subOperand);
                    case STARTS_OPERATOR -> itemName.startsWith(subOperand);
                    case ENDS_OPERATOR -> itemName.endsWith(subOperand);
                    case TAG_INDICATOR -> {
                        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(subOperand), Material.class);
                        if (tag == null) {
                            tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(subOperand), Material.class);
                        }
                        yield tag != null && tag.isTagged(itemStack.getType());
                    }
                    case ENCHANT_INDICATOR -> {
                        // Get `itemStack`'s enchantments
                        Map<Enchantment, Integer> itemStackEnchantmentMap;
                        if (itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
                            itemStackEnchantmentMap = ((EnchantmentStorageMeta) itemStack.getItemMeta()).getStoredEnchants();
                        } else {
                            itemStackEnchantmentMap = itemStack.getEnchantments();
                        }

                        // Parse `subOperand` for enchantment name and level
                        final String[] patternEnchantmentInfo = subOperand.split(String.valueOf(ENCHANT_LEVEL_DELIMITER));
                        if (patternEnchantmentInfo.length > 2 || patternEnchantmentInfo.length < 1) yield false;

                        final boolean isLevelScrupulous;
                        if (patternEnchantmentInfo.length == 1) {
                            isLevelScrupulous = false;
                        } else isLevelScrupulous = patternEnchantmentInfo[1].matches("-?\\d+");

                        final Enchantment patternEnchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(patternEnchantmentInfo[0]));
                        if (patternEnchantment == null) yield false;

                        final Integer itemStackEnchantmentLevel = itemStackEnchantmentMap.get(patternEnchantment);
                        if (!isLevelScrupulous) {
                            yield itemStackEnchantmentLevel != null;
                        } else {
                            Integer patternEnchantmentLevel = Integer.valueOf(patternEnchantmentInfo[1]);
                            yield itemStackEnchantmentLevel != null && itemStackEnchantmentLevel.equals(patternEnchantmentLevel);
                        }
                    }
                };
                if (permitted) {
                    orSubOperandResultantBoolean = true;
                } else {
                    continue nextPatternGroup;
                }
            }
            if (orSubOperandResultantBoolean) return false;
        }
        return true;
    }
    //endregion

    //region Event Handlers
    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        final Inventory destinationInventory = event.getInventory();
        final Item item = event.getItem();

        if (!destinationInventory.getType().equals(InventoryType.HOPPER)) return;
        final InventoryHolder destinationObject = destinationInventory.getHolder(false);

        if (ProhibitedItemsEx.get(item.hashCode()) != null && ProhibitedItemsEx.get(item.hashCode()) == destinationInventory.hashCode()) {
            event.setCancelled(true);
            return;
        }

        String destinationObjectName;
        if (destinationObject instanceof Hopper hopper) {
            destinationObjectName = FilteredHoppersPlugin.serialiseComponent(hopper.customName());
        } else if (destinationObject instanceof HopperMinecart hopperMinecart) {
            destinationObjectName = FilteredHoppersPlugin.serialiseComponent(hopperMinecart.customName());
        } else {
            return;
        }

        if (destinationObjectName == null) return;

        if (isItemProhibited(item.getItemStack(), destinationObjectName)) {
            event.setCancelled(true);

            ProhibitedItemsEx.put(item.hashCode(), destinationInventory.hashCode());
            Scheduler.runTaskLaterAsynchronously(Plugin, () -> ProhibitedItemsEx.remove(item.hashCode()), 20L * DiscardedItemCacheEntryExpiry);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(final InventoryMoveItemEvent event) {
        final Inventory destinationInventory = event.getDestination();
        if (!destinationInventory.getType().equals(InventoryType.HOPPER)) return;

        final ItemStack itemStack = event.getItem();
        final InventoryHolder destinationObject = destinationInventory.getHolder(false);

        final String destinationObjectName;
        if (destinationObject instanceof Hopper hopper) {
            destinationObjectName = FilteredHoppersPlugin.serialiseComponent(hopper.customName());
        } else if (destinationObject instanceof HopperMinecart hopperMinecart) {
            destinationObjectName = FilteredHoppersPlugin.serialiseComponent(hopperMinecart.customName());
        } else {
            return;
        }

        if (destinationObjectName == null) return;

        if (isItemProhibited(itemStack, destinationObjectName)) {
            event.setCancelled(true);

            // We need to wait one tick thanks to some silly ass behavior from Spigot
            // https://www.spigotmc.org/threads/inventorymoveitemevent-getsource-getcontents-always-returns-1.581448/#post-4615761
            Bukkit.getScheduler().runTaskLater(Plugin, () -> {
                final Inventory sourceInventory = event.getSource();
                final ItemStack[] sourceInventoryContents = event.getSource().getContents();

                final int sourceSlot = getNearestOccupiedSlot(sourceInventoryContents);
                final ItemStack sourceSlotItemStack = sourceInventoryContents[getNearestOccupiedSlot(sourceInventoryContents)];

                assert sourceSlotItemStack != null;

                // Do this to stop clogging the damn hoppers
                final int furthestEmptySlot = getFurthestEmptySlot(sourceInventoryContents);
                if (sourceSlot != sourceInventoryContents.length - 1 && sourceSlot != furthestEmptySlot + 1) {
                    sourceInventory.setItem(sourceSlot, null);
                    sourceInventory.setItem(furthestEmptySlot, sourceSlotItemStack);
                }
            }, 1);
        }
    }
    //endregion
}
