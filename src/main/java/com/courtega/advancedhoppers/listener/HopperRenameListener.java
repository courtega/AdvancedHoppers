package com.courtega.advancedhoppers.listener;

import com.courtega.advancedhoppers.AdvancedHoppersPlugin;
import com.courtega.advancedhoppers.Messenger;
import com.moandjiezana.toml.Toml;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HopperRenameListener implements Listener {
    public final static String CANCEL_COMMAND = ":nvm";
    private final static Map<Player, Hopper> HOPPER_RENAME_INTERACTIONS = new HashMap<>();
    private final AdvancedHoppersPlugin Plugin;
    private final Messenger Msger;
    private final BukkitScheduler Scheduler;
    private final int CancellationRadius;
    private final int InputPromptTimeout;

    public HopperRenameListener(AdvancedHoppersPlugin plugin) {
        this.Plugin = plugin;
        this.Scheduler = plugin.getServer().getScheduler();
        this.Msger = new Messenger(plugin);
        final Toml tomlConfig = plugin.getTomlConfig();

        this.CancellationRadius = Math.toIntExact(tomlConfig.getTable("Preferences").getLong("cancellation_radius"));
        this.InputPromptTimeout = Math.toIntExact(tomlConfig.getTable("Preferences").getLong("input_prompt_timeout"));
    }

    // region Event Handlers

    private void abortRenameOperation(Player player, Hopper hopper) {
        if (HOPPER_RENAME_INTERACTIONS.remove(player) == null) return;

        player.playSound(hopper.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3F, new Random().nextFloat(1.25F, 1.5F));
        //player.sendMessage(Messaging.RENAME_CANCELLED);
        Msger.sendInputPromptCancelled(player);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Block patientBlock = event.getClickedBlock();
        if (patientBlock == null) return;

        if (!(patientBlock.getState() instanceof final Hopper hopper)) return;

        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        final Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        event.setCancelled(true);

        final ItemStack itemStack = event.getItem();
        int err;
        if (itemStack == null) {
            err = renameHopper(player, hopper);
        } else {
            final BlockBreakEvent blockBreakEvent = new BlockBreakEvent(hopper.getBlock(), player);
            if (!blockBreakEvent.callEvent()) return;

            err = renameHopper(player, hopper, itemStack.getType().getKey().getKey());
        }

        if (err == 0) return;

        System.err.println("Something went wrong when performing the hopper rename action.");
    }

    // If player walks too far from the hopper, cancel the rename
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Hopper hopper = HOPPER_RENAME_INTERACTIONS.get(player);
        if (hopper == null) return;

        final Location hopperLocation = hopper.getLocation();
        final double distanceFromHopper = event.getTo().distance(hopperLocation);

        if (distanceFromHopper > CancellationRadius) {
            abortRenameOperation(player, hopper);
        }
    }

    // endregion

    // region Internal Logic

    // If the source of the event is a player who was prompted to rename a hopper, use that input to rename the hopper.
    @EventHandler
    public void onAsyncChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final Hopper hopper = HOPPER_RENAME_INTERACTIONS.get(player);
        if (hopper == null) return;

        event.setCancelled(true);

        final String originalMessage = AdvancedHoppersPlugin.serialiseComponent(event.originalMessage());
        if (originalMessage.equals(CANCEL_COMMAND)) {
            abortRenameOperation(player, hopper);
        } else {
            HOPPER_RENAME_INTERACTIONS.remove(player);
            renameHopper(player, hopper, originalMessage);
        }
    }

    // Prompt `player` to rename `hopper` based on their input from chat.
    private int renameHopper(final Player player, final Hopper hopper) {
        HOPPER_RENAME_INTERACTIONS.put(player, hopper);
        player.playSound(hopper.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, new Random().nextFloat(0.55F, 1.25F));
        //player.sendMessage(Messaging.REQUEST_CHAT_INPUT);
        Msger.sendInputPrompt(player);

        Scheduler.runTaskLater(Plugin, () -> abortRenameOperation(player, hopper), 20L * InputPromptTimeout);
        return 0;
    }

    // Overload: Rename `hopper` to `name`
    private int renameHopper(final Player player, final Hopper hopper, final String name) {
        final Component textComponent = name.equals(InventoryActionListener.RESET_COMMAND) ? null : Component.text(name);
        hopper.customName(textComponent);

        Plugin.getServer().getRegionScheduler().run(Plugin, hopper.getLocation(), _ -> hopper.update());

        player.playSound(hopper.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.75F, new Random().nextFloat(1.25F, 1.5F));
        //player.sendMessage(Messaging.EXPR_SET(name));
        Msger.sendExpressionSet(player, name);
        return 0;
    }

    //endregion
}
