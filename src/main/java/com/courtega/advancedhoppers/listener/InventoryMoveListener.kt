package com.courtega.advancedhoppers.listener

import com.courtega.advancedhoppers.AdvancedHoppers
import org.bukkit.Bukkit
import org.bukkit.block.Hopper
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitScheduler

class InventoryMoveListener(private val plugin: AdvancedHoppers) : Listener {

    // TODO: private val ProhibitedPickupItems: MutableMap<Int, Int> = HashMap()
    private val parser: ExpressionParser = ExpressionParser()
    private val scheduler: BukkitScheduler = Bukkit.getScheduler()

    @EventHandler
    fun onInventoryMoveItem(event: InventoryMoveItemEvent) {
        val destinationInventory: Inventory = event.destination
        if (destinationInventory.type != InventoryType.HOPPER) return

        val itemStack: ItemStack = event.item
        val destinationObject: InventoryHolder? = destinationInventory.holder

        val destinationObjectName: String = when (destinationObject) {
            is Hopper -> AdvancedHoppers.serialiseComponent(destinationObject.customName())
            is HopperMinecart -> AdvancedHoppers.serialiseComponent(destinationObject.customName())
            else -> return
        } ?: return

        if (parser.isItemProhibited(itemStack, destinationObjectName)) {
            event.isCancelled = true

            // We need to wait one tick thanks to some silly ass behavior from Spigot
            // https://www.spigotmc.org/threads/inventorymoveitemevent-getsource-getcontents-always-returns-1.581448/#post-4615761

            scheduler.runTaskLater(plugin, Runnable {
                val sourceInventory: Inventory = event.source
                val sourceInventoryContents = event.source.contents
                val sourceSlot: Int = getNearestOccupiedSlot(sourceInventoryContents)
                val sourceSlotItemStack: ItemStack = sourceInventoryContents[sourceSlot]!!

                val furthestEmptySlot: Int = getFurthestEmptySlot(sourceInventoryContents)
                if (sourceSlot != sourceInventoryContents.size - 1 && sourceSlot != furthestEmptySlot + 1) {
                    sourceInventory.setItem(sourceSlot, null)
                    sourceInventory.setItem(furthestEmptySlot, sourceSlotItemStack)
                }
            }, 1)
        }
    }

    private fun getFurthestEmptySlot(inventoryContents: Array<ItemStack?>): Int {
        val inventoryLength: Int = inventoryContents.size
        var slotPosition = 0
        for (i in inventoryLength downTo 1) {
            val slotContents: ItemStack? = inventoryContents[i - 1]
            if (slotContents == null) {
                slotPosition = i - 1
                break
            }
        }
        return slotPosition
    }

    private fun getNearestOccupiedSlot(inventoryContents: Array<ItemStack?>): Int {
        val inventoryLength: Int = inventoryContents.size
        var slotPosition: Int = inventoryLength - 1
        for (i in 0 until (inventoryLength - 1)) {
            val slotContents = inventoryContents[i]
            if (slotContents != null) {
                slotPosition = i
                break
            }
        }
        return slotPosition
    }
}