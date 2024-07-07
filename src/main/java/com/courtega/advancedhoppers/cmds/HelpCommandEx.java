package com.courtega.advancedhoppers.cmds;

import com.courtega.advancedhoppers.FilteredHoppersPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class HelpCommandEx implements CommandExecutor {
    private final static LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final FileConfiguration Config;

    public HelpCommandEx(FilteredHoppersPlugin plugin) {
        this.Config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        switch (args.length) {
            case 0 -> {
                sender.sendMessage(COMPONENT_SERIALIZER.deserialize("&7[&r    &d&lAdvancedHoppers&r by courtega    &7]&r\n"));

                sender.sendMessage(COMPONENT_SERIALIZER.deserialize("&n# Syntax Reference&r\n"));
                for (String k : Config.getKeys(true)) {
                    String v = (String) Config.get(k);
                    sender.sendMessage(COMPONENT_SERIALIZER.deserialize(k + " &7=&r " + "&b" + v + "&r"));
                }
                return true;
            }
        }
        return false;
    }
}
