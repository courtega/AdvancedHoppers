package com.courtega.advancedhoppers.cmds;

import com.courtega.advancedhoppers.AdvancedHoppersPlugin;
import com.courtega.advancedhoppers.Messenger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommandExecutor implements CommandExecutor {
    private final static LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final Messenger Msger;

    public HelpCommandExecutor(AdvancedHoppersPlugin plugin) {
        this.Msger = new Messenger(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length == 0) {
            Msger.sendHelp(player);
            return true;
        }
        return false;
    }
}
