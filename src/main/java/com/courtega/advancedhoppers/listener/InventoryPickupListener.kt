package com.courtega.advancedhoppers.listener

import com.courtega.advancedhoppers.AdvancedHoppers
import org.bukkit.Bukkit
import org.bukkit.block.Hopper
import org.bukkit.entity.Item
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitScheduler

class InventoryPickupListener(private val plugin: AdvancedHoppers) : Listener {

    private val prohibitedPickupItems: MutableMap<Int, Int> = HashMap()
    private val parser: ExpressionParser = ExpressionParser()
    private val scheduler: BukkitScheduler = Bukkit.getScheduler()
    private val discardItemCacheLifetime: Long = plugin.tomlConfig.getTable("Technical").getLong("cache_entry_expiry")

    @EventHandler
    fun onInventoryPickupItem(event: InventoryPickupItemEvent) {
        val destinationInventory: Inventory = event.inventory
        val item: Item = event.item

        if (destinationInventory.type != InventoryType.HOPPER) return
        val destinationObject: InventoryHolder? = destinationInventory.holder

        if (prohibitedPickupItems[item.hashCode()] != null && prohibitedPickupItems[item.hashCode()] == destinationInventory.hashCode()) {
            event.isCancelled = true
            return
        };

        val destinationObjectName: String = when (destinationObject) {
            is Hopper -> AdvancedHoppers.serialiseComponent(destinationObject.customName())
            is HopperMinecart -> AdvancedHoppers.serialiseComponent(destinationObject.customName())
            else -> return
        } ?: return

        if (parser.isItemProhibited(item.itemStack, destinationObjectName))
            event.isCancelled = true
        prohibitedPickupItems[item.hashCode()] = destinationInventory.hashCode()
        scheduler.runTaskLaterAsynchronously(
            plugin,
            Runnable { prohibitedPickupItems.remove(item.hashCode()) },
            20L * discardItemCacheLifetime
        )
    }

}