package com.aurora.profile;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import org.bukkit.Bukkit;

public class DonateLogic  extends ListenerAdapter {
    private final Profile plugin;
    public DonateLogic(Profile plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("donate")) {
            if (event.getUser().getId().equals("572836789593833472")) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Донат услуги");
                embed.setDescription("Выберите услугу из списка ниже\n" + plugin.getConfig().getString("emojiDisc1") + " **VIP** ```• Префикс ★ в чате и табе\n" +
                        "• Роль спонсора\n" +
                        "• Команды /pscale, /co i, /co near, /crawl\n" +
                        "• Доступ к /hat```\n" + plugin.getConfig().getString("emojiDisc2") + " **UNMUTE**\n```• Снятие ограничения чата\n" +
                        "• По нику```\n" + plugin.getConfig().getString("emojiDisc1") + "**GIF**\n ```• Кастомная гифка в /profile\n• ВАЖНОЕ ПРИМЕЧАНИЕ: вы должны использовать прямую ссылку на gif(в конце должно быть .gif), в противном случае гифка не будет установлена```");

                StringSelectMenu menu = StringSelectMenu.create("donation")
                        .addOption("VIP", "vip", Emoji.fromFormatted(plugin.getConfig().getString("emojiDisc1")))
                        .addOption("UNMUTE", "unmute", Emoji.fromFormatted(plugin.getConfig().getString("emojiDisc2")))
                        .addOption("GIF", "gif", Emoji.fromFormatted(plugin.getConfig().getString("emojiDisc3")))
                        .build();

                ActionRow actionRow = ActionRow.of(menu);

                event.replyEmbeds(embed.build())
                        .addComponents(actionRow)
                        .queue();
            }
            else {
                event.reply("хаха лох").setEphemeral(true).queue();
            }
        }
        else if (event.getName().equals("addmoney")) {
            if (event.getUser().getId().equals("572836789593833472") || event.getUser().getId().equals("770926642163744778")) {
                String discordId = event.getOption("discordid").getAsString();
                int howMuch = event.getOption("скока").getAsInt();
                int current = plugin.getDonateMap().getOrDefault(discordId, 0);
                plugin.getDatabase().updateDonate(discordId, current + howMuch);
                plugin.getDonateMap().put(discordId, current + howMuch);
                event.reply("Успешно! Теперь на счету у <@" + discordId + "> вот столько много деняк: " + plugin.getDonateMap().get(discordId)).setEphemeral(true).queue();
            }
        }
        else if (event.getName().equals("removemoney")) {
            if (event.getUser().getId().equals("572836789593833472") || event.getUser().getId().equals("770926642163744778")) {
                String discordId = event.getOption("discordid").getAsString();
                int howMuch = event.getOption("скока").getAsInt();
                int current = plugin.getDonateMap().getOrDefault(discordId, 0);
                plugin.getDatabase().updateDonate(discordId, current - howMuch);
                plugin.getDonateMap().put(discordId, current - howMuch);
                event.reply("Успешно! Теперь на счету у <@" + discordId + "> вот столько много деняк: " + plugin.getDonateMap().get(discordId)).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("donation")) {
            String selected = event.getValues().get(0);

            switch (selected) {
                case "vip":
                    TextInput nickname = TextInput.create("nickname", TextInputStyle.SHORT)
                            .setPlaceholder("Введите никнейм кто получит донат")
                            .setMinLength(3)
                            .setMaxLength(30)
                            .build();
                    Modal modal = Modal.create("vipName", "Донат VIP")
                            .addComponents(
                                    Label.of("Ник: ", nickname)
                            )
                            .build();
                    event.replyModal(modal).queue();
                    break;
                case "unmute":
                    TextInput nicknameUnmute = TextInput.create("nickname", TextInputStyle.SHORT)
                            .setPlaceholder("Введите никнейм кто получит анмут")
                            .setMinLength(3)
                            .setMaxLength(30)
                            .build();
                    Modal modalUnmute = Modal.create("unmuteName", "Анмут")
                            .addComponents(
                                    Label.of("Ник: ", nicknameUnmute)
                            )
                            .build();
                    event.replyModal(modalUnmute).queue();
                    break;
                case "gif":
                    TextInput gif = TextInput.create("whatGif", TextInputStyle.SHORT)
                            .setPlaceholder("Введите ссылку на гиф из дискорда")
                            .setMinLength(5)
                            .setMaxLength(100)
                            .build();
                    Modal modalGif = Modal.create("gifLink", "Гифка")
                            .addComponents(
                                    Label.of("Гифка:", gif)
                            )
                            .build();
                    event.replyModal(modalGif).queue();
                    break;
            }
        }


    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("vipName")) {
            String nickname = event.getValue("nickname").getAsString();
            String discordId = event.getUser().getId();
            int current = plugin.getDonateMap().get(discordId);
            if (current >= 300) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "lp user" + nickname + "parent addtemp vip 30d"
                    );
                });
                plugin.getDonateMap().put(discordId, current - 300);
                plugin.getDatabase().updateDonate(discordId, current - 300);
                event.reply("Донат для `" + nickname + "` приобретён успешно! \nНа вашем счету: " + plugin.getDonateMap().get(discordId)).setEphemeral(true).queue();
            }
            else {
                event.reply("Недостаточно средств :_(\nОднако всегда можно пополнить счёт!)").setEphemeral(true).queue();
            }
        }
        else if (event.getModalId().equals("unmuteName")) {
            String nickname = event.getValue("nickname").getAsString();
            String discordId = event.getUser().getId();
            int current = plugin.getDonateMap().get(discordId);
            if (current >= 100) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "unmute" + nickname
                    );
                });
                plugin.getDonateMap().put(discordId, current - 100);
                plugin.getDatabase().updateDonate(discordId, current - 100);
                event.reply("Донат для `" + nickname + "` приобретён успешно! \nНа вашем счету: " + plugin.getDonateMap().get(discordId)).setEphemeral(true).queue();
            }
            else {
                event.reply("Недостаточно средств :_(\nОднако всегда можно пополнить счёт!)").setEphemeral(true).queue();
            }
        }
        else if (event.getModalId().equals("gifLink")) {
            String gif = event.getValue("whatGif").getAsString();
            String discordId = event.getUser().getId();
            int current = plugin.getDonateMap().get(discordId);
            if (current >= 100) {
                plugin.getDatabase().addOrUpdateGif(gif, discordId);
                plugin.addProfileGifs(discordId, gif);
                plugin.getDonateMap().put(discordId, current - 100);
                plugin.getDatabase().updateDonate(discordId, current - 100);
                event.reply("Донат приобретён успешно! \nНа вашем счету: " + plugin.getDonateMap().get(discordId)).setEphemeral(true).queue();
            }
            else {
                event.reply("Недостаточно средств :_(\nОднако всегда можно пополнить счёт!)").setEphemeral(true).queue();
            }
        }
    }
}
