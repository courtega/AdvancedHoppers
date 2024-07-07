package com.courtega.advancedhoppers;

import com.courtega.advancedhoppers.cmds.HelpCommandEx;
import com.courtega.advancedhoppers.cmds.HelpTabComplete;
import com.courtega.advancedhoppers.listener.HopperRenameListenerEx;
import com.courtega.advancedhoppers.listener.InventoryActionListenerEx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public final class FilteredHoppersPlugin extends JavaPlugin {

    public Logger logger;

    public static @Nullable String serialiseComponent(final Component component) {
        return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Override
    public void onEnable() {
        registerListeners(
                new HopperRenameListenerEx(this),
                new InventoryActionListenerEx(this)
        );

        this.saveDefaultConfig();

        PluginCommand rootCommand = this.getCommand("advancedhoppers");
        assert rootCommand != null;

        rootCommand.setExecutor(new HelpCommandEx(this));
        rootCommand.setTabCompleter(new HelpTabComplete());

        logger = this.getLogger();
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
