package com.github.redreaperlp.reaperutility.util;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.command.handler.LButton;
import com.github.redreaperlp.reaperutility.command.handler.LSelection;
import com.github.redreaperlp.reaperutility.enums.EPermission;
import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.scheduler.EventScheduler;
import com.github.redreaperlp.reaperutility.user.UserStorage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.*;
import java.util.List;

public class Embedder {
    public static MessageEmbed eventConfigurationSent(User user, Message message) {
        return new EmbedBuilder()
                .setTitle("Event Configuration Sent! 📧")
                .setDescription("You can edit the event by clicking [here](" + message.getJumpUrl() + ")")
                .setColor(0x00ff00)
                .setAuthor(user.getName(), "https://discord.gg/ghhKXDGQhD", user.getAvatarUrl())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910314618880060/Mail_Icon.png")
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
    }

    public static MessageEmbed eventConfiguration(Configurator configurator) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Event Creation")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setTimestamp(Instant.now())
                .addField(EmbedField.NAME.toField((configurator.isEditing() ? "> " : "") + configurator.name(configurator.isEditing())))
                .addField(EmbedField.DESCRIPTION.toField(configurator.description(false)))
                .addField(EmbedField.LOCATION.toField(configurator.location(false)))
                .addField(EmbedField.DATE.toField(configurator.date()));
        if (!configurator.isEditing()) {
            builder.addField(EmbedField.NOTIFICATION.toField(configurator.notification()));
            builder.addField(EmbedField.REMAINING.toField(configurator.remaining()));
            builder.addField(EmbedField.EVENT_SEND_CHANNEL.toField(configurator.guildChannel(false)));
        } else {
            GuildMessageChannel channel = (GuildMessageChannel) configurator.guildChannel();
            builder.setTitle("Event Editing", channel.retrieveMessageById(configurator.messageID()).complete().getJumpUrl());
        }
        return builder.setFooter("Stuck? Click the❓button to get help!")
                .setColor(0x00ff00)
                .build();
    }

    public static List<ActionRow> eventConfigurationActionRow(Configurator configurator) {
        boolean enableButton = false;
        if (configurator != null) {
            enableButton = (configurator.isValid() == Configurator.FailReason.SUCCESS);
        } else {
            Color.RED.printInfo("Configurator is null!");
        }
        return List.of(ActionRow.of(
                        LButton.EButton.CANCEL_EVENT.getButton(),
                        configurator.isEditing() ?
                                LButton.EButton.EDIT_EVENT.getButton(enableButton) :
                                LButton.EButton.COMPLETE_EVENT.getButton(enableButton),
                        LButton.EButton.HELP_EVENT.getButton()
                ),
                ActionRow.of(
                        StringSelectMenu.create(LSelection.ESelectMenu.EVENT_CREATION.cName())
                                .addOptions(
                                        LSelection.ESelectOptions.EVENT_NAME.option(),
                                        LSelection.ESelectOptions.EVENT_DESCRIPTION.option(),
                                        LSelection.ESelectOptions.EVENT_DATE.option(),
                                        LSelection.ESelectOptions.EVENT_LOCATION.option()
                                ).build()
                )
        );
    }

    public static MessageEmbed rateLimitEmbed(UserStorage storage) {
        return new EmbedBuilder()
                .setTitle("Rate Limit")
                .setDescription("You are being rate limited! Please wait " + TimeFormat.RELATIVE.format(LocalDateTime.now().toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).plusSeconds(storage.rateLimit() * 6L)) + " seconds before trying again!")
                .setTimestamp(Instant.now())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setColor(0xff0000)
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
    }

    public static MessageEmbed unknownCommandEmbed() {
        return new EmbedBuilder()
                .setTitle("Unknown Command")
                .setDescription("The command you entered is unknown! Please report this to the developer [here](https://discord.gg/ghhKXDGQhD)!")
                .setTimestamp(Instant.now())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setColor(0xff0000)
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
    }

    public static MessageEmbed noConfiguratorAvailable() {
        return new EmbedBuilder()
                .setTitle("No Configurator Available")
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setDescription("There is no configurator available! Please create one!")
                .setColor(0xFF0000)
                .setFooter("Dont know how to continue? Click the❓button to get help!")
                .build();
    }

    public static MessageEmbed noPermission(EPermission permission, Guild guild, User user) {
        new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.GREEN).append(" tried to use something without permission: ", Color.GRAY)
                .append(permission.get(), Color.RED).printWarning();
        return new EmbedBuilder()
                .setTitle("No Permission")
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910078680895519/Event_Icon.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setDescription("You have insufficient permissions to execute this command! If you think this is a mistake, please contact the [server owner](https://discordapp.com/users/" + guild.getOwner().getId() + ")!")
                .addField("Permission", "`" + permission.get() + "`", true)
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setColor(0xFF0000)
                .build();
    }

    public static MessageEmbed notFound(String whatNotFound) {
        return new EmbedBuilder()
                .setTitle(whatNotFound + " not found")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setDescription("The " + whatNotFound + " you entered was not found! Please try again!")
                .setColor(0xFF0000)
                .setFooter("Dont know how to continue? Click the❓button to get help!")
                .build();
    }

    public static MessageEmbed eventAlreadyStarted(String name, boolean edit) {
        return new EmbedBuilder()
                .setTitle("Event already started")
                .setDescription("The event `" + name + "` has already started!" + (edit ? "\nYou can no longer modify this Event!" : "\nYou can no longer `accept` `decline` `unsure` this event!"))
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                .setColor(0xFF0000)
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .build();
    }

    public static MessageEmbed eventAlreadyStarted(String name) {
        return eventAlreadyStarted(name, false);
    }

    public static MessageEmbed eventEmbed(EventScheduler.EventStorage storage) {
        String name = storage.name();
        String description = storage.description();
        String location = storage.location();
        String date = storage.date(false);
        String remaining = TimeFormat.RELATIVE.format(Instant.ofEpochSecond(Long.parseLong(date.split(":")[1])));
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(name);
        if (description != null) builder.addField(EmbedField.DESCRIPTION.toField(description));
        if (location != null) builder.addField(EmbedField.LOCATION.toField(location));
        builder.addField(EmbedField.DATE.toField(date));
        builder.addField(EmbedField.REMAINING.toField(remaining));
        builder.addField(EmbedField.EVENT_ACCEPTED.toField(String.join("\n", storage.getAccepted()), true));
        builder.addField(EmbedField.EVENT_UNSURE.toField(String.join("\n", storage.getUnsure()), true));
        builder.addField(EmbedField.EVENT_DECLINED.toField(String.join("\n", storage.getDeclined()), true));
        builder.setColor(0xc8ff00);
        builder.setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl());
        return builder.build();
    }

    public static MessageEmbed failReason(Configurator.FailReason reason, User user) {
        return new EmbedBuilder()
                .setTitle("Event configuration")
                .addField("This is not valid.", "`" + reason.getReason() + "`", false)
                .setAuthor(user.getAsTag(), "https://discord.gg/ghhKXDGQhD", user.getAvatarUrl())
                .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setColor(0xFF0000).build();
    }

    public static MessageEmbed notifyUsers(EventScheduler.EventStorage storage, String url) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Event Reminder \uD83D\uDCEC", url);
        builder.addField("Event: ", storage.name(), false);
        if (storage.description() != null)
            builder.addField(EmbedField.DESCRIPTION.toField(MarkdownUtil.quote(storage.description())));
        if (storage.location() != null)
            builder.addField(EmbedField.LOCATION.toField(MarkdownUtil.quote(storage.location())));
        builder.addField(EmbedField.DATE.toField(MarkdownUtil.quote(storage.date(false))));
        builder.addField(EmbedField.REMAINING.toField(MarkdownUtil.quote(TimeFormat.RELATIVE.format(Instant.ofEpochSecond(Long.parseLong(storage.date(false).split(":")[1]))))));
        builder.setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084910314618880060/Mail_Icon.png")
                .setAuthor(Main.jda.getSelfUser().getName(), "https://discord.gg/ghhKXDGQhD", Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setImage("https://cdn.discordapp.com/attachments/1060601917878829226/1079493198004621412/Event_Reminder.png")
                .setFooter(Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getEffectiveAvatarUrl())
                .setColor(0x00ff00);
        return builder.build();
    }

    public enum EmbedField {
        NAME("Name", "The name of the event"),
        DESCRIPTION("Description", "The description of the event"),
        LOCATION("Location", "The location of the event"),
        DATE("Date", "The date of the event"),
        NOTIFICATION("Notification", "The notification of the event"),
        REMAINING("Remaining", "The remaining time until start of the event"),
        EVENT_SEND_CHANNEL("Event Channel", "The channel to send the event to"),
        EVENT_ACCEPTED("Accepted", "-"),
        EVENT_DECLINED("Declined", "-"),
        EVENT_UNSURE("Unsure", "-"),
        NONE("None", "None");
        private final String name;
        private final String description;

        EmbedField(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static EmbedField get(String name) {
            for (EmbedField value : values()) {
                if (value.name.equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return NONE;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public MessageEmbed.Field toField(String value, boolean inline) {
            return new MessageEmbed.Field(name, value, inline);
        }

        public MessageEmbed.Field toField(String value) {
            return new MessageEmbed.Field(name, value, false);
        }
    }
}
