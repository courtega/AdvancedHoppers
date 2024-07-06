package com.courtega.advancedhoppers.listener;

import com.courtega.advancedhoppers.FilteredHoppersPlugin;
import com.courtega.advancedhoppers.Messaging;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HopperRenameListenerEx implements Listener {
    public final static String CANCEL_COMMAND = ":nvm";
    private static final Map<Player, Hopper> HOPPER_RENAME_INTERACTIONS = new HashMap<>();
    private static final int CANCELLATION_RADIUS = 9;
    private final FilteredHoppersPlugin Plugin;

    public HopperRenameListenerEx(FilteredHoppersPlugin plugin) {
        this.Plugin = plugin;
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

        if (distanceFromHopper > CANCELLATION_RADIUS) {
            HOPPER_RENAME_INTERACTIONS.remove(player);
            player.playSound(hopper.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3F, new Random().nextFloat(1.25F, 1.5F));
            player.sendMessage(Messaging.RENAME_CANCELLED);
        }
    }

    // If the source of the event is a player who was prompted to rename a hopper, use that input to rename the hopper.
    @EventHandler
    public void onAsyncChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final Hopper hopper = HOPPER_RENAME_INTERACTIONS.get(player);
        if (hopper == null) return;

        event.setCancelled(true);
        HOPPER_RENAME_INTERACTIONS.remove(player);

        final String originalMessage = FilteredHoppersPlugin.serialiseComponent(event.originalMessage());
        if (originalMessage.equals(CANCEL_COMMAND)) {
            player.playSound(hopper.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3F, new Random().nextFloat(1.25F, 1.5F));
            player.sendMessage(Messaging.RENAME_CANCELLED);
            return;
        }

        renameHopper(player, hopper, originalMessage);
    }

    // Prompt `player` to rename `hopper` based on their input from chat.
    private int renameHopper(final Player player, final Hopper hopper) {
        HOPPER_RENAME_INTERACTIONS.put(player, hopper);
        player.playSound(hopper.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, new Random().nextFloat(0.55F, 1.25F));
        player.sendMessage(Messaging.REQUEST_CHAT_INPUT);
        return 0;
    }

    // Overload: Rename `hopper` to `name`
    private int renameHopper(final Player player, final Hopper hopper, final String name) {
        final Component textComponent = name.equals("all") ? null : Component.text(name);
        hopper.customName(textComponent);

        final FilteredHoppersPlugin filteredHoppersPlugin = Plugin;
        filteredHoppersPlugin.getServer().getRegionScheduler().run(filteredHoppersPlugin, hopper.getLocation(), task -> hopper.update());

        player.playSound(hopper.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.75F, new Random().nextFloat(1.25F, 1.5F));
        player.sendMessage(Messaging.EXPR_SET(name));
        return 0;
    }
}
