package com.courtega.advancedhoppers.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

// TODO: Yes I know this is horrendous I am going to rework this messaging crap later
public class HelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendRichMessage(
                    """
                            <light_purple>AdvancedHoppers</light_purple> <gray>by</gray> courtega
                            <i><gray>Rewrite of jwkerr's HopperFilter</gray></i>"""
            );
            return true;
        } else if (args.length == 1 && args[0].equals("help")) {
            sender.sendRichMessage(
                    """
                                        
                            <light_purple>AdvancedHoppers</light_purple> <gray>by</gray> courtega
                            <i><gray>Rewrite of jwkerr's HopperFilter</gray></i>
                                        
                            <u>Syntax Guide</u>
                                                        
                            not: <aqua>!</aqua>
                            and: <aqua>&</aqua>
                            or: <aqua>|</aqua>
                            has: <aqua>*</aqua>
                            tag: <aqua>#</aqua>
                            enchant: <aqua>+</aqua>
                            starts with: <aqua>^</aqua>
                            ends with: <aqua>$</aqua>
                            """
            );
            return true;
        }
        return false;
    }
}
