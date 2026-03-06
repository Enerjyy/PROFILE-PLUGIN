package com.aurora.profile;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.time.LocalDate;

public class DiscordDayLogin  extends ListenerAdapter implements Listener {
    private final Profile plugin;
    public DiscordDayLogin(Profile plugin) {
        this.plugin = plugin;
    }

    private Map<String, String> joinedToday = new HashMap<>();
    private Map<String, Boolean> messagedToday = new HashMap<>();



    @EventHandler
    public void onPlayerJoinFirstByDay(AsyncPlayerPreLoginEvent event) {
        String uuidString = event.getUniqueId().toString();
        String ip = event.getAddress().getHostAddress();

        if (joinedToday.containsKey(uuidString)) return;


        String savedIp = joinedToday.get(uuidString);
        if (savedIp != null && ip.equals(savedIp)) return;



        UUID playerUUID = event.getUniqueId();
        String nickname = event.getName();
        String discordId = plugin.getDiscordLinkMap().get(nickname);
        String today = LocalDateTime.now().toString();



        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "Стой!\nПодтверди, что это ты в лс в Discord!"
        );


        if (Boolean.TRUE.equals(messagedToday.get(uuidString))) return;


        messagedToday.put(playerUUID.toString(), true);



        plugin.getJda().retrieveUserById(discordId).queue(user1 -> {

            String message = plugin.getConfig().getString("tryingToLogIn");

            Button buttonAccept = Button.success("aaccept" + "|" + playerUUID.toString() + "|" + today + "|" + ip, "")
                    .withEmoji((Emoji.fromFormatted("<:green:1471577470712025262>")));
            Button buttonNotAccept = Button.danger("disaccept" + "|" + playerUUID.toString(), "")
                    .withEmoji((Emoji.fromFormatted("<:red:1471577450604400691>>")));
            ActionRow actionRow = ActionRow.of(buttonAccept, buttonNotAccept);

            user1.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage(message)
                        .addComponents(actionRow)
                        .queue();
            });

        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();

        if (buttonId.startsWith("aaccept")) {
            String[] parts = buttonId.split("\\|");
            String pUUID = parts[1];
            String pIP = parts[3];

            joinedToday.put(pUUID, pIP);
            event.getMessage().delete().queue();
            event.reply("Ты подтвердил вход!").setEphemeral(true).queue();
            messagedToday.put(pUUID, false);


        }
        else if (buttonId.startsWith("disaccept")) {
            String[] parts = buttonId.split("\\|");
            String pUUID = parts[1];
            event.getMessage().delete().queue();
            event.reply("Ты отменил подтверждение!").setEphemeral(true).queue();
            messagedToday.put(pUUID, false);
        }

    }
}
