package com.github.redreaperlp.reaperutility.command.handler;

import com.github.redreaperlp.reaperutility.enums.ECommand;
import com.github.redreaperlp.reaperutility.enums.EPermission;
import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.scheduler.EventScheduler;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.storage.server.permission.JPermission;
import com.github.redreaperlp.reaperutility.user.UserManager;
import com.github.redreaperlp.reaperutility.user.UserStorage;
import com.github.redreaperlp.reaperutility.util.Embedder;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.event.JEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

public class LButton extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        UserStorage storage = UserManager.getUser(event.getUser());
        if (!UserManager.isUserRateLimited(storage)) {
            EButton button = EButton.get(event.getComponentId());
            switch (button) {
                case COMPLETE_EVENT, EDIT_EVENT -> {
                    event.deferReply().setEphemeral(true).queue();
                    try {
                        Configurator configurator = storage.currentConfigurator();
                        if (configurator == null) {
                            configurator = new Configurator(event.getMessage());
                        }
                        if (button == EButton.EDIT_EVENT) {
                            JGuild server = JStorage.instance().getGuild(false, Long.parseLong(event.getMessage().getEmbeds().get(0).getUrl().replace("https://discord.com/channels/", "").split("/")[0]));
                            JEvent jEvent = server.getEvent(Long.parseLong(configurator.messageID()));
                            if (jEvent == null || jEvent.getRemainingTime() < 0) {
                                event.getHook().sendMessageEmbeds(Embedder.eventAlreadyStarted(configurator.name(true), true)).queue();
                                event.getMessage().delete().queue();
                                return;
                            }
                        }
                        Guild guild = configurator.guildChannel().getGuild();
                        Member member = event.getMember();
                        if (member == null) {
                            member = guild.retrieveMember(event.getUser()).complete();
                            if (member == null) {
                                event.getHook().sendMessageEmbeds(Embedder.failReason(Configurator.FailReason.NOT_PART_OF_GUILD, event.getUser())).queue();
                                return;
                            }
                        }
                        boolean hasPermission = false;
                        JGuild server = JStorage.instance().getGuild(true, configurator.guildChannel().getGuild().getIdLong());
                        JPermission permission = server.getPermission(button.permission());
                        if (permission != null) {
                            hasPermission = permission.hasPermission(member);
                        }
                        if (!hasPermission) {
                            event.getHook().sendMessageEmbeds(Embedder.noPermission(button.permission, guild, event.getUser())).setEphemeral(true).queue();
                            return;
                        }
                        complete(event, configurator, storage, member);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case CANCEL_EVENT -> {
                    event.deferEdit().queue();
                    storage.currentConfigurator(null);
                    event.getMessage().delete().queue();
                }
                case HELP_EVENT -> {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Help")
                            .setDescription("This is the help message for creating an event.")
                            .addField("What is an event?", "An event is a message that will be sent to a channel and reminds users who want to participate at a specific time.\n" +
                                    "Users can choose whether they join or not", false)
                            .addField("Name", "As the name suggests, this is the name of the event. It will be displayed in the message.", false)
                            .addField("Description", "This is the description of the event. It will also be displayed in the message. (enter `none` to reset)", false)
                            .addField("Location", "This is the location of the event. It will be displayed in the message as well.\nCan be displayed as " + event.getChannel().getAsMention() + " or as blank text (enter `none` to reset)", false)
                            .addField("Date", "This is the date of the event. And also this will be displayed in the message.", false)
                            .addField("Notification", "This is the roles to notify of the event on send. You might know, but this is also represented in the message, but you have to use following command to set it: \n`/select-notify-role <roleID/Name>`", false)
                            .addField("Time", "This is the time of the event. And yes, also this will be displayed in the message.", false)
                            .addField("Channel", "This is the channel to send the message to. Same thing here, but you have to use following command to set it: \n`/select-send-channel <channelID/Name>`", false)
                            .setImage("https://cdn.discordapp.com/attachments/1060601917878829226/1084743724258570332/HelpBanner.png")
                            .setColor(0xFFFF00)
                            .setAuthor(event.getUser().getName(), "https://discord.gg/ghhKXDGQhD", event.getUser().getEffectiveAvatarUrl())
                            .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909944039559269/Help.png")
                            .build()
                    ).setEphemeral(true).queue();
                }
                case HELP_EVENT_CREATE -> {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Help")
                            .setDescription("This is the help message for how to start creating an event.")
                            .addField("How to start?", "To start creating an event, you have to use the following command: \n`/" + ECommand.EVENT.getName() + "` in a guild, you have the permission to execute", false)
                            .addField("What can you do?", "After you started the creation, you will receive a message with a selection menu!\n" +
                                    "You can also use the following commands to set the values: \n" +
                                    "`/" + ECommand.SELECT_ROLE_NOTIFY.getName() + " <role>`\n" +
                                    "`/" + ECommand.SELECT_SEND_CHANNEL.getName() + " <channel>`\n", false)
                            .addField("Need more info?", "Click the button below to get more information about the creation process", false)
                            .setImage("https://cdn.discordapp.com/attachments/1060601917878829226/1084743724258570332/HelpBanner.png")
                            .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909944039559269/Help.png")
                            .setColor(0xFFFF00)
                            .setAuthor(event.getUser().getName(), "https://discord.gg/ghhKXDGQhD", event.getUser().getEffectiveAvatarUrl()
                            ).build()).addActionRow(EButton.HELP_EVENT.getButton()).setEphemeral(true).queue();
                }
                case HELP_PERMISSIONS -> {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Help")
                            .setDescription("This is the help message for how to set the permissions for the event command.")
                            .addField("How to set?", "To set the permissions, you have to use the following command: \n`/" + ECommand.SET_PERMISSION.getName() + " <command> <role>`", false)
                            .addField("<command>", "This is the command you want to set the permissions for. You can autocomplete the command with tab.", false)
                            .addField("<role>", "This is the role you want to set the permissions for. You can autocomplete the role with tab. \n> ✅ -> has permission\n> ❌ -> has not permission", false)
                            .setImage("https://cdn.discordapp.com/attachments/1060601917878829226/1084743724258570332/HelpBanner.png")
                            .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909944039559269/Help.png")
                            .setAuthor(event.getUser().getName(), "https://discord.gg/ghhKXDGQhD", event.getUser().getEffectiveAvatarUrl())
                            .setColor(0xFFFF00)
                            .build()
                    ).setEphemeral(true).queue();
                }
                case ACCEPT_EVENT -> {
                    EventScheduler.EventStorage eventStorage = new EventScheduler.EventStorage(event.getMessage(), true);
                    if (eventStorage.remainingSeconds() <= 0) {
                        event.replyEmbeds(Embedder.eventAlreadyStarted(eventStorage.name())).setEphemeral(true).queue();
                    } else {
                        if (eventStorage.addAccepted(event.getUser()))
                            event.editMessageEmbeds(Embedder.eventEmbed(eventStorage)).queue();
                        else event.deferEdit().queue();
                    }
                }
                case DECLINE_EVENT -> {
                    EventScheduler.EventStorage eventStorage = new EventScheduler.EventStorage(event.getMessage(), true);
                    if (eventStorage.remainingSeconds() <= 0) {
                        event.replyEmbeds(Embedder.eventAlreadyStarted(eventStorage.name())).setEphemeral(true).queue();
                    } else {
                        eventStorage.addDeclined(event.getUser());
                        event.editMessageEmbeds(Embedder.eventEmbed(eventStorage)).queue();
                    }
                }
                case UNSURE_EVENT -> {
                    EventScheduler.EventStorage eventStorage = new EventScheduler.EventStorage(event.getMessage(), true);
                    if (eventStorage.remainingSeconds() <= 0) {
                        event.replyEmbeds(Embedder.eventAlreadyStarted(eventStorage.name())).setEphemeral(true).queue();
                    } else {
                        eventStorage.addUnsure(event.getUser());
                        event.editMessageEmbeds(Embedder.eventEmbed(eventStorage)).queue();
                    }
                }
                case DELETE_EVENT -> {
                    try {
                        Member member = event.getGuild().getMember(event.getUser());
                        if (member == null) member = event.getGuild().retrieveMember(event.getUser()).complete();
                        if (JStorage.instance().getGuild(true, event.getGuild().getIdLong()).getPermission(button.permission).hasPermission(member)) {
                            event.getMessage().delete().queue();
                            EventScheduler.removeEvent(event.getMessage());
                            event.deferEdit().queue();
                        } else {
                            event.replyEmbeds(Embedder.noPermission(button.permission, event.getGuild(), event.getUser())).setEphemeral(true).queue();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case MODIFY_EVENT -> {
                    try {
                        event.deferReply().setEphemeral(true).queue();
                        JEvent jEvent = JStorage.instance().getGuild(true, event.getGuild().getIdLong()).getEvent(event.getMessage().getIdLong());
                        if (jEvent == null) {
                            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("Error")
                                    .setDescription("This Event is not valid anymore!")
                                    .setColor(0xFF0000)
                                    .setThumbnail("https://cdn.discordapp.com/attachments/1084909692037373992/1084909806348939365/Error.png")
                                    .setAuthor(event.getUser().getName(), "https://discord.gg/ghhKXDGQhD", event.getUser().getEffectiveAvatarUrl())
                                    .setFooter(event.getJDA().getSelfUser().getName(), event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                                    .build()).queue();
                            return;
                        }
                        Member member = event.getMember();
                        if (member == null) member = event.getGuild().retrieveMember(event.getUser()).complete();
                        if (JStorage.instance().getGuild(true, event.getGuild().getIdLong()).getPermission(button.permission).hasPermission(member)) {
                            try {
                                Configurator configurator = new Configurator(event.getMessage(), true);
                                Message message = event.getUser().openPrivateChannel().complete().sendMessageEmbeds(Embedder.eventConfiguration(configurator)).addComponents(Embedder.eventConfigurationActionRow(configurator)).complete();
                                event.getHook().sendMessageEmbeds(Embedder.eventConfigurationSent(event.getUser(), message)).queue();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            event.getHook().sendMessageEmbeds(Embedder.noPermission(button.permission, event.getGuild(), event.getUser())).queue();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (!storage.isNotified()) {
                event.replyEmbeds(Embedder.rateLimitEmbed(storage)).setEphemeral(true).queue();
            } else {
                event.deferEdit().queue();
            }
        }
    }

    private void complete(ButtonInteractionEvent event, Configurator configurator, UserStorage storage, Member member) {
        JStorage.instance().getGuild(true, configurator.guildChannel().getGuild().getIdLong()).getPermission(EButton.COMPLETE_EVENT.permission()).hasPermission(member);
        Configurator.FailReason reason = configurator.isValid();
        if (reason == Configurator.FailReason.SUCCESS) {
            event.getMessage().delete().queue();
            Message message = EventScheduler.schedule(new EventScheduler.EventStorage(event.getMessage()), configurator.notification());
            storage.currentConfigurator(null);
            if (configurator.isEditing()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Event edited")
                        .setDescription("The event has been sent to the channel [here](" + message.getJumpUrl() + ")")
                        .setColor(0x00FF00)
                        .setAuthor(event.getUser().getAsTag(), "https://discord.gg/ghhKXDGQhD", event.getUser().getAvatarUrl())
                        .build()
                ).setEphemeral(true).queue();
            } else {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Event created")
                        .setDescription("The event has been sent to the channel [here](" + message.getJumpUrl() + ")")
                        .setColor(0x00FF00)
                        .setAuthor(event.getUser().getAsTag(), "https://discord.gg/ghhKXDGQhD", event.getUser().getAvatarUrl())
                        .build()
                ).setEphemeral(true).queue();
            }
        } else {
            event.getHook().sendMessageEmbeds(Embedder.failReason(reason, event.getUser())).setEphemeral(true).queue();
            event.getMessage().editMessageComponents(Embedder.eventConfigurationActionRow(configurator)).queue();
        }
    }


    public enum EButton {
        COMPLETE_EVENT("complete-event", "Complete", ButtonStyle.SUCCESS, EPermission.EVENT_CREATE),
        CANCEL_EVENT("cancel-event", "Cancel", ButtonStyle.DANGER),
        HELP_EVENT("help-event", "❓", ButtonStyle.SECONDARY),
        HELP_EVENT_CREATE("help-create-event", "❓", ButtonStyle.SECONDARY),
        HELP_PERMISSIONS("help-set-permission", "❓", ButtonStyle.SECONDARY),

        ACCEPT_EVENT("accept-event", "✅Accept", ButtonStyle.SUCCESS),
        DECLINE_EVENT("decline-event", "❌Decline", ButtonStyle.PRIMARY),
        UNSURE_EVENT("unsure-event", "❔Unsure", ButtonStyle.SECONDARY),
        DELETE_EVENT("delete-event", "ㅤㅤ🗑️Delete ㅤㅤ", ButtonStyle.DANGER, EPermission.EVENT_DELETE),
        MODIFY_EVENT("modify-event", "ㅤㅤ 📝Edit ㅤㅤ", ButtonStyle.PRIMARY, EPermission.EVENT_EDIT),
        EDIT_EVENT("edit-complete", "Complete", ButtonStyle.SUCCESS, EPermission.EVENT_EDIT),
        ;

        private final String name;
        private final String label;
        private final ButtonStyle style;
        private EPermission permission;

        EButton(String name, String label, ButtonStyle style) {
            this.name = name;
            this.label = label;
            this.style = style;
        }

        EButton(String name, String label, ButtonStyle style, EPermission permission) {
            this.name = name;
            this.label = label;
            this.style = style;
            this.permission = permission;
        }

        public String cName() {
            return name;
        }

        public String label() {
            return label;
        }

        public Button getButton() {
            return Button.of(style, name, label);
        }

        public Button getButton(boolean enabled) {
            Button button = Button.of(style, name, label);
            if (enabled) {
                button = button.asEnabled();
            } else {
                button = button.asDisabled();
            }
            return button;
        }

        public EPermission permission() {
            return permission;
        }

        public static EButton get(String name) {
            for (EButton button : values()) {
                if (button.name.equals(name)) return button;
            }
            return null;
        }
    }
}
