package com.courtega.advancedhoppers.listener

import com.courtega.advancedhoppers.AdvancedHoppers
import com.courtega.advancedhoppers.Messenger
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Hopper
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitScheduler
import java.util.*
import java.util.logging.Level

// Get ready... this is gonna be complicated
class ExpressionEditListener(private val plugin: AdvancedHoppers) : Listener {

    private val messenger: Messenger = Messenger(plugin)
    private val scheduler: BukkitScheduler = plugin.server.scheduler

    private val cancelCommand: String = ":nvm"

    private val random: Random = Random()
    private val soundNames: List<String> = Sound.entries.map { s -> s.toString() }
    private val editPromptSound: String
    private val editConfirmSound: String
    private val editCancelSound: String

    private val cancellationRadius: Int
    private val inputPromptTimeout: Int

    private val hopperEditInteractions: MutableMap<Player, Hopper> = HashMap()
    private val hopperMinecartEditInteractions: MutableMap<Player, HopperMinecart> = HashMap()

    // region Event Handlers
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val patientBlock: Block = event.clickedBlock ?: return
        if (patientBlock.state !is Hopper) return
        val hopper: Hopper = patientBlock.state as Hopper
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (!player.isSneaking) return

        event.isCancelled = true

        val itemStack: ItemStack? = event.item

        val err: Int
        if (itemStack == null) {
            err = renameHopper(player, hopper)
        } else {
            // Protection plugin compatibility
            val blockBreakEvent = BlockBreakEvent(hopper.block, player)
            if (!blockBreakEvent.callEvent()) return

            err = renameHopper(player, hopper, itemStack.type.key.key)
        }

        if (err == 0) return

        plugin.logger.log(Level.SEVERE, "Something went wrong while trying to edit a hopper expression.")
    }

    @EventHandler
    fun onEntityDamageByEntity(event: VehicleDamageEvent) {
        val whoInflicted: Entity? = event.attacker
        if (whoInflicted !is Player) return
        val player: Player = whoInflicted

        val patientEntity: Entity = event.vehicle
        if (patientEntity !is HopperMinecart) return
        val hopperMinecraft: HopperMinecart = patientEntity

        if (!player.isSneaking) return

        event.isCancelled = true

        val itemStack: ItemStack = player.inventory.itemInMainHand

        val err: Int
        if (itemStack.type == Material.AIR) {
            err = renameHopperMinecart(player, hopperMinecraft)
        } else {
            // Protection plugin compatibility
            val playerInteractEntityEvent = PlayerInteractEntityEvent(whoInflicted, patientEntity)
            if (!playerInteractEntityEvent.callEvent()) return

            err = renameHopperMinecart(player, hopperMinecraft, itemStack.type.key.key)
        }

        if (err == 0) return

        plugin.logger.log(Level.SEVERE, "Something went wrong while trying to edit a hopper expression.")
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player: Player = event.player
        val hopper: Hopper = hopperEditInteractions[player] ?: return

        val hopperLocation: Location = hopper.location
        val distanceFromHopper: Double = event.to.distance(hopperLocation)

        if (distanceFromHopper > cancellationRadius) {
            abortExpressionEdit(player, hopper)
        }
    }

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val player: Player = event.player
        val hopperObject = when (hopperEditInteractions[player]) {
            is Hopper -> hopperEditInteractions[player]
            else -> hopperMinecartEditInteractions[player]
        } ?: return

        event.isCancelled = true

        val playerInput: String = AdvancedHoppers.serialiseComponent(event.originalMessage()) ?: cancelCommand

        if (playerInput != cancelCommand) {

            when (hopperObject) {
                is Hopper -> {
                    hopperEditInteractions.remove(player)
                    renameHopper(player, hopperObject, playerInput)
                }

                is HopperMinecart -> {
                    hopperMinecartEditInteractions.remove(player)
                    renameHopperMinecart(player, hopperObject, playerInput)
                }
            }
        } else {
            when (hopperObject) {
                is Hopper -> abortExpressionEdit(player, hopperObject)
                is HopperMinecart -> abortExpressionEdit(player, hopperObject)
            }
        }

    }
    // endregion

    // region Private Methods
    private fun renameHopper(player: Player, hopper: Hopper): Int {
        hopperEditInteractions[player] = hopper
        if (soundNames.contains(editPromptSound)) {
            val sound: Sound = Sound.valueOf(editPromptSound)
            player.playSound(hopper.location, sound, 0.1f, random.nextFloat(0.55f, 1.25f))
        }

        messenger.sendInputPrompt(player)
        scheduler.runTaskLater(plugin, Runnable { abortExpressionEdit(player, hopper) }, 20L * inputPromptTimeout)

        return 0
    }

    private fun renameHopper(player: Player, hopper: Hopper, expression: String): Int {
        val textComponent: Component? =
            if (expression == ExpressionParser().resetCommand) null else Component.text(expression)
        hopper.customName(textComponent)

        plugin.server.regionScheduler.run(plugin, hopper.location) { hopper.update() }

        if (soundNames.contains(editConfirmSound)) {
            val sound: Sound = Sound.valueOf(editConfirmSound)
            player.playSound(hopper.location, sound, 0.75f, random.nextFloat(1.25f, 1.5f))
        }

        messenger.sendExpressionSet(player, expression)

        return 0
    }

    private fun renameHopperMinecart(player: Player, hopperMinecart: HopperMinecart): Int {
        hopperMinecartEditInteractions[player] = hopperMinecart
        if (soundNames.contains(editPromptSound)) {
            val sound: Sound = Sound.valueOf(editPromptSound)
            player.playSound(hopperMinecart.location, sound, 0.1f, random.nextFloat(0.55f, 1.25f))
        }

        messenger.sendInputPrompt(player)
        scheduler.runTaskLater(
            plugin,
            Runnable { abortExpressionEdit(player, hopperMinecart) },
            20L * inputPromptTimeout
        )

        return 0
    }

    private fun renameHopperMinecart(player: Player, hopperMinecart: HopperMinecart, expression: String): Int {
        val textComponent: Component? =
            if (expression == ExpressionParser().resetCommand) null else Component.text(expression)
        hopperMinecart.customName(textComponent)
        hopperMinecart.isCustomNameVisible = false

        if (soundNames.contains(editConfirmSound)) {
            val sound: Sound = Sound.valueOf(editConfirmSound)
            player.playSound(hopperMinecart.location, sound, 0.75f, random.nextFloat(1.25f, 1.5f))
        }

        messenger.sendExpressionSet(player, expression)

        return 0
    }

    private fun abortExpressionEdit(player: Player, hopper: Hopper) {
        if (hopperEditInteractions.remove(player) == null) return

        if (soundNames.contains(editCancelSound)) {
            val sound: Sound = Sound.valueOf(editCancelSound)
            player.playSound(hopper.location, sound, 0.3f, random.nextFloat(1.25f, 1.5f))
        }

        messenger.sendInputPromptCancelled(player)
    }

    private fun abortExpressionEdit(player: Player, hopperMinecart: HopperMinecart) {
        if (hopperMinecartEditInteractions.remove(player) == null) return

        if (soundNames.contains(editCancelSound)) {
            val sound: Sound = Sound.valueOf(editCancelSound)
            player.playSound(hopperMinecart.location, sound, 0.3f, random.nextFloat(1.25f, 1.5f))
        }

        messenger.sendInputPromptCancelled(player)
    }

    // endregion
    init {
        val config = plugin.tomlConfig
        this.editPromptSound = config.getTable("Audio").getString("expression_edit_sound")
        this.editConfirmSound = config.getTable("Audio").getString("expression_confirm_sound")
        this.editCancelSound = config.getTable("Audio").getString("expression_cancel_sound")
        this.cancellationRadius = Math.toIntExact(config.getTable("Preferences").getLong("cancellation_radius"))
        this.inputPromptTimeout = Math.toIntExact(config.getTable("Preferences").getLong("input_prompt_timeout"))
    }
}