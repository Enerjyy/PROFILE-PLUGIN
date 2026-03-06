package com.aurora.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ManhuntEventCommand implements CommandExecutor {
    private final Profile plugin;
    public ManhuntEventCommand(Profile plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startevent")) {
            if (!sender.isOp()) {
                return true;
            }
            plugin.getManhuntEvent().startEvent();
            return true;
        }
        if (command.getName().equalsIgnoreCase("event")) {
            boolean isActive = plugin.getEventMap().get(sender.getName());
            plugin.addEventMap(sender.getName(), !isActive);
            plugin.getDatabase().addOrUpdateEventActivity(sender.getName(), !isActive);
            String message1 = plugin.getConfig().getString("activeInEvent");
            String message2 = plugin.getConfig().getString("inactiveInEvent");
            MiniMessage mm = MiniMessage.miniMessage();
            Component message2Comp = mm.deserialize(message1);
            Component message1Comp = mm.deserialize(message2);

            if (isActive) {
                sender.sendMessage(message2Comp);
            }
            else {
                sender.sendMessage(message1Comp);
            }
            return true;
        }
        return false;
    }

}
