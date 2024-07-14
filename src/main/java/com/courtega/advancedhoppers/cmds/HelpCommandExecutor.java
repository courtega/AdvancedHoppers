package com.courtega.advancedhoppers.cmds;

import com.courtega.advancedhoppers.AdvancedHoppers;
import com.courtega.advancedhoppers.Messenger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommandExecutor implements CommandExecutor {
    private final Messenger Msger;

    public HelpCommandExecutor(AdvancedHoppers plugin) {
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
