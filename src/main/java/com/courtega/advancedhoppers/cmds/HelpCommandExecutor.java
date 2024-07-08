package com.courtega.advancedhoppers.cmds;

import com.courtega.advancedhoppers.listener.InventoryActionListener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HelpCommandExecutor implements CommandExecutor {
    private final static LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(COMPONENT_SERIALIZER.deserialize("&7[&r    &d&lAdvancedHoppers&r by courtega    &7]&r\n"));

            sender.sendMessage(COMPONENT_SERIALIZER.deserialize("&n# Syntax Reference&r\n"));
            for (Map.Entry<String, String> kvp : InventoryActionListener.getConstants().entrySet()) {
                sender.sendMessage(COMPONENT_SERIALIZER.deserialize(kvp.getKey() + " &7=&r " + "&b" + kvp.getValue() + "&r"));
            }
            return true;
        }
        return false;
    }
}
