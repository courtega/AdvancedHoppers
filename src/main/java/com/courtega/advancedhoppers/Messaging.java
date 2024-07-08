package com.courtega.advancedhoppers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import static com.courtega.advancedhoppers.listener.HopperRenameListener.CANCEL_COMMAND;

// TODO: Yes I know this is horrendous I am going to rework this messaging crap later
public class Messaging {
    public static final TextComponent PREFIX =
            Component.text('[', NamedTextColor.GRAY)
                    .append(Component.text("AdvancedHoppers", NamedTextColor.DARK_PURPLE))
                    .append(Component.text("] ", NamedTextColor.GRAY));
    public static final TextComponent REQUEST_CHAT_INPUT =
            PREFIX
                    .append(Component.text("Enter a filter expression, or enter \""))
                    .append(Component.text(CANCEL_COMMAND).color(NamedTextColor.AQUA).decorate(TextDecoration.ITALIC))
                    .append(Component.text("\" to cancel."));
    public static final TextComponent RENAME_CANCELLED =
            PREFIX
                    .append(Component.text("Rename action aborted."));

    public static TextComponent EXPR_SET(String expression) {
        return
                PREFIX
                        .append(Component.text("Filter expression set to \""))
                        .append(Component.text(expression, NamedTextColor.DARK_GREEN))
                        .append(Component.text("\""));
    }
}
