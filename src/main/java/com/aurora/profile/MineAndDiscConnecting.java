package com.aurora.profile;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MineAndDiscConnecting extends ListenerAdapter implements org.bukkit.event.Listener {

    private final Map<String, String> pendingCodes = new HashMap<>();
    private final Random random = new Random();

    private final Profile plugin;
    public MineAndDiscConnecting(Profile plugin) {
        this.plugin = plugin;
    }

    public boolean isLinked(String minecraftNick) {
        return plugin.getDiscordLinkMap().containsKey(minecraftNick);
    }

    public String getDiscordId(String nickname) {
        return plugin.getDiscordLinkMap().get(nickname);
    }

    public void link (String nickname, String discordId) {
        plugin.addDiscordLinkMap(nickname, discordId);
        plugin.addMinecraftLinkMap(discordId, nickname);
        plugin.getDatabase().setDiscord(nickname, discordId);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        String nickname = event.getName();
        String uuid = event.getUniqueId().toString();

        if (!plugin.getDatabase().playerExists(nickname)) {
            plugin.getDatabase().addEverything(uuid, nickname);
        }

        if (!isLinked(nickname)) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Подайте заявку у нас в дискорде! \ndiscord.gg/MdybFX3er3");
        }
    }

    /*
    public String generateCode(String minecraftNick) {
        String code = String.format("%06d", random.nextInt(1_000_000)); // 6 цифр
        pendingCodes.put(minecraftNick, code); // ключ = ник
        return code;
    }


    public String verifyCode(String code, String discordId) {
        String nick = null;
        for (Map.Entry<String, String> entry : pendingCodes.entrySet()) {
            if (entry.getValue().equals(code)) {
                nick = entry.getKey();
                break;
            }
        }

        if (nick != null) {
            pendingCodes.remove(nick);
            link(nick, discordId);
        }

        return nick;
    }


    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        String nickname = event.getName();
        String uuid = event.getUniqueId().toString();

        if (!plugin.getDatabase().playerExists(nickname)) {
            plugin.getDatabase().addEverything(uuid, nickname);
        }

        if (!isLinked(nickname)) {
            String code = generateCode(nickname);
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Ваш дискорд не привязан..\nВаш код: " + code
            );
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromGuild()) return;

        String code = event.getMessage().getContentRaw().trim();
        String discordId = event.getAuthor().getId();

        if (plugin.getDiscordLinkMap().containsValue(discordId)) {
            event.getChannel().sendMessage("⚠️ Этот Discord уже привязан к аккаунту!").queue();
            return;
        }

        String nick = verifyCode(code, discordId);

        if (nick != null) {
            event.getChannel().sendMessage("✅ Аккаунт `" + nick + "` успешно привязан к Discord!").queue();
        } else {
            event.getChannel().sendMessage("❌ Код неверный или уже использован").queue();
        }
    }
     */

}
