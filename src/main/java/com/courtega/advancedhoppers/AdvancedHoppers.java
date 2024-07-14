package com.courtega.advancedhoppers;

import com.courtega.advancedhoppers.cmds.HelpCommandExecutor;
import com.courtega.advancedhoppers.dependencies.Metrics;
import com.courtega.advancedhoppers.listener.ExpressionEditListener;
import com.courtega.advancedhoppers.listener.InventoryMoveListener;
import com.courtega.advancedhoppers.listener.InventoryPickupListener;
import com.moandjiezana.toml.Toml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AdvancedHoppers extends JavaPlugin {
    private final static String TOML_CONFIG = "config.toml";
    private final static String TOML_LOCALE = "locale.toml";

    public Logger logger;

    public static @Nullable String serialiseComponent(final Component component) {
        return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Override
    public void onEnable() {
        final int pluginId = 22638;
        Metrics metrics = new Metrics(this, pluginId);

        registerListeners(
                // new HopperRenameListener(this),
                new ExpressionEditListener(this),
                new InventoryMoveListener(this),
                new InventoryPickupListener(this)
        );

        this.getTomlConfig();

        PluginCommand rootCommand = this.getCommand("advancedhoppers");
        assert rootCommand != null;

        rootCommand.setExecutor(new HelpCommandExecutor(this));

        logger = this.getLogger();
    }

    public Toml getTomlConfig() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                logger.log(Level.SEVERE, "Failed to create data folder.");
            }
        }

        File tomlConfig = new File(getDataFolder(), TOML_CONFIG);
        if (!tomlConfig.exists()) {
            try {
                InputStream inputStream = getResource(TOML_CONFIG);
                assert inputStream != null;
                Files.copy(inputStream, tomlConfig.toPath());
            } catch (final IOException e) {
                this.logger.log(Level.SEVERE, "Artifact missing default configuration!", e);
            }
        }
        return new Toml().read(getResource(TOML_CONFIG)).read(tomlConfig);
    }

    public Toml getTomlLocale() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                logger.log(Level.SEVERE, "Failed to create data folder.");
            }
        }

        File tomlConfig = new File(getDataFolder(), TOML_LOCALE);
        if (!tomlConfig.exists()) {
            try {
                InputStream inputStream = getResource(TOML_LOCALE);
                assert inputStream != null;
                Files.copy(inputStream, tomlConfig.toPath());
            } catch (final IOException e) {
                this.logger.log(Level.SEVERE, "Artifact missing default configuration!", e);
            }
        }
        return new Toml().read(getResource(TOML_LOCALE)).read(tomlConfig);
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
