package com.aurora.profile;

import kotlin.uuid.Uuid;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.awt.*;
import java.io.File;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Discord  extends ListenerAdapter {
    Color color_pink = Color.decode("#DB7093");
    Color color_red = Color.decode("#E32636");
    Color color_green = Color.decode("#47A76A");
    private final Profile plugin;
    public Discord(Profile plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getCustomId();
        User user = event.getUser();
        String userId = user.getId();

        if (buttonId.startsWith("zayavka")) {
            TextInput nickname = TextInput.create("nickname", TextInputStyle.SHORT)
                    .setPlaceholder("Введите ник Майнкрафт")
                    .setMinLength(3)
                    .setMaxLength(30)
                    .build();
            TextInput age = TextInput.create("age", TextInputStyle.SHORT)
                    .setPlaceholder("Введите свой возраст")
                    .setMinLength(2)
                    .setMaxLength(2)
                    .build();
            TextInput fromWhereDidUKnowBoutUs = TextInput.create("where",TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Откуда вы узнали о проекте?")
                    .setMinLength(3)
                    .setMaxLength(100)
                    .build();
            Modal modal = Modal.create("ticket", "Заявка")
                    .addComponents(
                            Label.of("Ник:", nickname),
                            Label.of("Возраст:", age),
                            Label.of("Откуда узнали:", fromWhereDidUKnowBoutUs)
                    )
                    .build();

            event.replyModal(modal).queue();
        }

        else if (buttonId.startsWith("AccessFor") || buttonId.startsWith("DeclineFor")) {

            if (buttonId.startsWith("AccessFor")) {
                String[] parts = buttonId.split("\\|");
                String nickname = parts[1];
                String discordId = parts[2];
                nickname.substring(0, nickname.length()-1);



                UUID uuidU = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + nickname).getBytes(StandardCharsets.UTF_8)
                );
                String uuid = uuidU.toString();

                if (!plugin.getDatabase().playerExists(nickname)) {
                    plugin.getDatabase().addEverything(uuid, nickname);
                }

                plugin.addDiscordLinkMap(nickname, discordId);
                plugin.addMinecraftLinkMap(discordId, nickname);
                plugin.getDatabase().setDiscord(nickname, discordId);


                plugin.getJda().retrieveUserById(discordId).queue(user1 -> {

                    String message = plugin.getConfig().getString("acceptingTicket")
                                    .replace("<player>", nickname);


                    user1.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage(message).queue();
                    });

                });


                Guild guild = plugin.getJda().getGuildById(plugin.getConfig().getString("guildId"));

                Role roleToAdd = guild.getRoleById(plugin.getConfig().getString("roleToAdd"));
                Role roleToRemove = guild.getRoleById(plugin.getConfig().getString("roleToRemove"));

                ArrayList<Role> rolesToAdd = new ArrayList<>();
                rolesToAdd.add(roleToAdd);
                ArrayList<Role> rolesToRemove = new ArrayList<>();
                rolesToRemove.add(roleToRemove);

                Member member = guild.getMemberById(discordId);

                if (member != null) {
                    guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
                }

                User administrator = event.getUser();
                String admId = administrator.getId();

                TextChannel logs = plugin.getJda().getTextChannelById(plugin.getConfig().getString("logsChannel"));

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Принятие игрока `" + nickname + "` - <@" + discordId + ">");
                embed.setDescription("Игрок `" + nickname + "` был принят администратором <@" + admId + ">");
                embed.setColor(color_green);

                logs.sendMessageEmbeds(embed.build()).queue();

                event.deferEdit().queue(success -> {
                    event.getMessage().delete().queue();
                });

            }
            else if (buttonId.startsWith("DeclineFor")) {
                String[] parts = buttonId.split("\\|");
                String discordId = parts[2];


                TextInput reason = TextInput.create("reason", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Введите причину отклонения заявки")
                        .setMinLength(5)
                        .setMaxLength(100)
                        .build();
                Modal modal = Modal.create("reasonForDecline|" + discordId + "|" + event.getMessage().getId(), "Причина отклонения заявки")
                        .addComponents(
                            Label.of("Причина:", reason)
                            )
                            .build();
                event.replyModal(modal).queue();





            }




        }

        if (buttonId.startsWith("vote1_") || buttonId.startsWith("vote2_")) {
            String nickname = buttonId.substring(6);
            nickname = nickname.replaceAll("\\d+$", "");
            String discordId = plugin.getDiscordLinkMap().get(nickname);

            if (discordId == null) {
                event.reply("Данный игрок ещё не заходил на сервер..").setEphemeral(true).queue();
                return;
            }




            if (buttonId.startsWith("vote1_")) {



                if (!plugin.hasVoted(discordId, userId)) {
                    plugin.addUpVotes(discordId, userId);
                    plugin.getDatabase().addOrUpdateVote(discordId, userId, "up");
                    int up = plugin.getUpVotedCount(discordId, userId);
                    int down = plugin.getDownVotedCount(discordId, userId);
                    int idGen = up + down + 1;
                    Button button1 = Button.success("vote1_" + nickname + idGen, String.valueOf(up))
                            .withEmoji(Emoji.fromFormatted("<:green:1471577470712025262>"));
                    event.editButton(button1)
                            .queue();

                }
                else if (plugin.getDownVotes().get(discordId).contains(userId) && plugin.hasVoted(discordId, userId)) {
                    plugin.addUpVotes(discordId, userId);
                    plugin.removeDownVotes(discordId, userId);
                    plugin.getDatabase().addOrUpdateVote(discordId, userId, "up");
                    int up = plugin.getUpVotedCount(discordId, userId);
                    int down = plugin.getDownVotedCount(discordId, userId);
                    int idGen = up + down + 1;
                    Button button1 = Button.success("vote1_" + nickname + idGen, String.valueOf(up))
                            .withEmoji(Emoji.fromFormatted("<:green:1471577470712025262>"));

                    Button button2 = Button.danger("vote2_" + nickname + idGen, String.valueOf(down))
                            .withEmoji(Emoji.fromFormatted("<:red:1471577450604400691>"));
                    ActionRow actionRow = ActionRow.of(button1, button2);
                    event.editComponents(actionRow)
                            .queue();
                }
                else {
                    event.reply("Вы уже голосовали за игрока " + nickname).setEphemeral(true).queue();
                }
            }
            if (buttonId.startsWith("vote2_")) {


                if (!plugin.hasVoted(discordId, userId)) {
                    plugin.addDownVotes(discordId, userId);
                    plugin.getDatabase().addOrUpdateVote(discordId, userId, "down");
                    int up = plugin.getUpVotedCount(discordId, userId);
                    int down = plugin.getDownVotedCount(discordId, userId);
                    int idGen = up + down + 1;
                    Button button2 = Button.danger("vote2_" + nickname + idGen, String.valueOf(down))
                            .withEmoji(Emoji.fromFormatted("<:red:1471577450604400691>"));
                    event.editButton(button2)
                            .queue();

                }
                else if (plugin.getUpVotes().get(discordId).contains(userId) && plugin.hasVoted(discordId, userId)) {
                    plugin.addDownVotes(discordId, userId);
                    plugin.removeUpVotes(discordId, userId);
                    plugin.getDatabase().addOrUpdateVote(discordId, userId, "down");
                    int up = plugin.getUpVotedCount(discordId, userId);
                    int down = plugin.getDownVotedCount(discordId, userId);
                    int idGen = up + down + 1;
                    Button button1 = Button.success("vote1_" + nickname + idGen, String.valueOf(up))
                            .withEmoji(Emoji.fromFormatted("<:green:1471577470712025262>"));
                    Button button2 = Button.danger("vote2_" + nickname + idGen, String.valueOf(down))
                            .withEmoji(Emoji.fromFormatted("<:red:1471577450604400691>"));
                    ActionRow actionRow = ActionRow.of(button1, button2);
                    event.editComponents(actionRow)
                            .queue();

                }
                else {
                    event.reply("Вы уже голосовали против игрока " + nickname).setEphemeral(true).queue();
                }
            }
        }



    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if(event.getModalId().startsWith("reasonForDecline")) {
            String modalId = event.getModalId();

            String[] parts = modalId.split("\\|");

            String discordId = parts[1];
            String messageId = parts[2];

            String reason = event.getValue("reason").getAsString();

            plugin.getJda().retrieveUserById(discordId).queue(user -> {

                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(
                            "Твоя заявка, к сожалению, была отклонена..\nПричина: " + reason
                    ).queue();
                });

            }, failure -> {
                System.out.println("Не удалось найти пользователя: " + discordId);
            });


            User administrator = event.getUser();
            String admId = administrator.getId();

            TextChannel logs = plugin.getJda().getTextChannelById("1472943741366173787");

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Игрок <@" + discordId + "> не был принят на сервер");
            embed.setDescription("Игрок <@" + discordId + "> не был принят администратором <@" + admId + ">\n```Причина:" + reason + "```");
            embed.setColor(color_red);

            logs.sendMessageEmbeds(embed.build()).queue();

            String channelId = event.getChannel().getId();
            TextChannel channelToDelete = plugin.getJda().getTextChannelById(channelId);
            channelToDelete.deleteMessageById(messageId).queue();

            event.reply("Успешно отклонено").setEphemeral(true).queue();

        }

        if (event.getModalId().equals("ticket")) {
            String ageStr = event.getValue("age").getAsString();
            String name = event.getValue("nickname").getAsString();
            String fromWhere = event.getValue("where").getAsString();
            User user = event.getUser();
            String userId = user.getId();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Новая заявка!");
            embed.addField("Никнейм", "`" + name + "`", false);
            embed.addField("Дискорд", "<@" + userId + ">", false);
            embed.addField("Возраст", ageStr, false);
            embed.addField("Откуда узнали", fromWhere, false);

            TextChannel channel = plugin.getJda().getTextChannelById("1472596570125107392");

            Random random = new Random();

            int idGen = random.nextInt(32000);

            int userIdLength = userId.length();

            Button buttonAccept = Button.success("AccessFor" + "|" + name + "|" + userId, "Принять")
                    .withEmoji(Emoji.fromFormatted("<:green:1471577470712025262>"));;
            Button buttonDecline = Button.danger("DeclineFor" + "|" + name + "|" + userId, "Отклонить")
                    .withEmoji(Emoji.fromFormatted("<:red:1471577450604400691>"));
            ActionRow actionRow = ActionRow.of(buttonAccept, buttonDecline);


            channel.sendMessageEmbeds(embed.build())
                    .addComponents(actionRow)
                    .queue();


            event.reply("Ваша заявка была успешно отправлена <:hp:1396497676123771071>")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("profile")) {
            String nickname = event.getOption("ник").getAsString();
            if (plugin.getDiscordLinkMap().get(nickname) == null) {
                event.reply(plugin.getConfig().getString("errorHasNotJoinedYet")).setEphemeral(true).queue();
                return;
            }
            plugin.getNetwork().downloadUpperBodyAsync(nickname)
                    .thenAccept(file -> {
                        if (!file.exists()) {
                            event.reply("❌ Картинка не найдена").setEphemeral(true).queue();
                            return;
                        }
                        Player player = Bukkit.getPlayerExact(nickname);
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle(" ");
                        if (player != null) {
                            int inTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                            double inHours = inTicks / 20 / 3600;
                            embed.setDescription("## Профиль `" + nickname + "` - <@" + plugin.getDiscordLinkMap().get(nickname) + ">\n### Онлайн <:aurora_dot:1396516769967116349>");
                        } else {
                            embed.setDescription("### Профиль `" + nickname + "` - <@" + plugin.getDiscordLinkMap().get(nickname) + ">\n### Оффлайн <:black_dot:1396497855874859141>");
                        }
                        if (plugin.getTimePlayeds().get(nickname) != null) {
                            embed.addField("Часов наиграно: ", plugin.getTimePlayeds().get(nickname), true);
                        } else {
                            embed.addField("Часов наиграно: ", "Неизвестно", true);
                        }

                        embed.addField("", "", true);

                        if (plugin.getDeaths().get(nickname) != null) {
                            embed.addField("Смертей: ", plugin.getDeaths().get(nickname), true);
                        } else {
                            embed.addField("Смертей: ", "Неизвестно", true);
                        }
                        if (plugin.getCoefsOfMining().get(nickname) != null) {
                            embed.addField("Шанс x2 руды:", plugin.getCoefsOfMining().get(nickname).toString(), true);
                        } else {
                            embed.addField("Шанс x2 руды:", "Неизвестно", true);
                        }
                        embed.addField("", "", true);
                        if (plugin.getCoefsOfWood().get(nickname) != null) {
                            embed.addField("Шанс x2 дерева:", plugin.getCoefsOfWood().get(nickname).toString(), true);
                        } else {
                            embed.addField("Шанс x2 дерева:", "Неизвестно", true);
                        }
                        embed.setColor(color_pink);
                        embed.setThumbnail("attachment://upper_body.png");
                        embed.setImage(plugin.getProfileGifs().get(plugin.getDiscordLinkMap().get(nickname)));


                        User user = event.getUser();
                        String userId = user.getId();

                        int up = plugin.getUpVotedCount(plugin.getDiscordLinkMap().get(nickname), userId);
                        int down = plugin.getDownVotedCount(plugin.getDiscordLinkMap().get(nickname), userId);


                        int idGen = up + down;
                        Button button1 = Button.success("vote1_" + nickname + idGen, String.valueOf(up))
                                .withEmoji(Emoji.fromFormatted("<:green:1471577470712025262>"));
                        Button button2 = Button.danger("vote2_" + nickname + idGen, String.valueOf(down))
                                .withEmoji(Emoji.fromFormatted("<:red:1471577450604400691>"));
                        ActionRow actionRow = ActionRow.of(button1, button2);

                        event.replyEmbeds(embed.build())
                                .addFiles(FileUpload.fromData(file, "upper_body.png"))
                                .addComponents(actionRow)
                                .queue();
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        event.reply("❌ Ошибка при загрузке головы игрока")
                                .setEphemeral(true)
                                .queue();
                        return null;
                    });
        }
        else if (event.getName().equals("tickets")) {
            if (event.getUser().getId().equals("572836789593833472")) {

                TextChannel channel = plugin.getJda().getTextChannelById(plugin.getConfig().getString("ticketChannelId"));
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(" ");
                embed.setDescription("### <:globus:1472880615270514688> Подать заявку на сервер \nДля доступа к серверу необходимо пройти верификацию:\n" +
                        "\n" +
                        "1. Нажмите кнопку ниже\n" +
                        "2. Заполните все поля анкеты\n" +
                        "3. Ожидайте проверки модератором\n" +
                        "\n" +
                        "После проверки вы получите уведомление в ЛС");
                embed.setImage("https://cdn.discordapp.com/attachments/1388842818461634684/1473062361396154572/blurred-image_2_1.png?ex=6994d7bb&is=6993863b&hm=fe5f9ff93ed922f41168dd16b13afcb7f1d373b68714f45a00b6b9f76ee10f31&");
                embed.setColor(color_pink);

                Button button = Button.secondary("zayavka", "Подать заявку")
                                .withEmoji((Emoji.fromFormatted("<:globus:1472880615270514688>")));
                ActionRow actionRow = ActionRow.of(button);

                channel.sendMessageEmbeds(embed.build())
                        .addComponents(actionRow)
                        .queue();

            }
            else {
                event.reply(plugin.getConfig().getString("errorNoPermission")).queue();
            }
        }
    }
}
