package com.courtega.advancedhoppers;

import com.courtega.advancedhoppers.listener.HopperRenameListener;
import com.courtega.advancedhoppers.listener.InventoryActionListener;
import com.moandjiezana.toml.Toml;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Map;


public class Messenger {

    private final static LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final static TextComponent PREFIX = COMPONENT_SERIALIZER.deserialize("&7[&dAdvancedHoppers&7]&r ");
    private final static TextComponent ANNOUNCE = COMPONENT_SERIALIZER.deserialize("\n&7[&r    &d&lAdvancedHoppers&r by courtega    &7]&r\n");
    private final Toml TomlLocale;

    public Messenger(AdvancedHoppersPlugin plugin) {
        this.TomlLocale = plugin.getTomlLocale();
    }

    public void sendHelp(Player player) {
        player.sendMessage(ANNOUNCE);

        String rawMsgHeader = TomlLocale.getTable("Commands").getString("syntax_reference");
        player.sendMessage(COMPONENT_SERIALIZER.deserialize(rawMsgHeader + '\n'));

        for (Map.Entry<String, String> kvp : InventoryActionListener.getConstants().entrySet()) {
            player.sendMessage(COMPONENT_SERIALIZER.deserialize(kvp.getKey() + " &7=&r " + "&b" + kvp.getValue() + "&r"));
        }
        player.sendMessage("");
    }

    public void sendInputPrompt(Player player) {
        String rawMessage = TomlLocale.getTable("General").getString("expression_prompt");
        TextComponent textComponent = PREFIX
                .append(COMPONENT_SERIALIZER.deserialize(String.format(rawMessage, HopperRenameListener.CANCEL_COMMAND)));
        player.sendMessage(textComponent);
    }

    public void sendInputPromptCancelled(Player player) {
        String rawMessage = TomlLocale.getTable("General").getString("expression_prompt_abort");
        TextComponent textComponent = PREFIX
                .append(COMPONENT_SERIALIZER.deserialize(String.format(rawMessage)));
        player.sendMessage(textComponent);
    }

    public void sendExpressionSet(Player player, String expression) {
        String rawMessage = TomlLocale.getTable("General").getString("expression_set");
        TextComponent textComponent = PREFIX
                .append(COMPONENT_SERIALIZER.deserialize(String.format(rawMessage, expression)));
        player.sendMessage(textComponent);
    }

}
