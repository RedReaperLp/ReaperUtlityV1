package com.github.redreaperlp.reaperutility.command.handler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.enums.ECommand;
import com.github.redreaperlp.reaperutility.enums.EPermission;
import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.storage.server.permission.JPermission;
import com.github.redreaperlp.reaperutility.user.UserManager;
import com.github.redreaperlp.reaperutility.user.UserStorage;
import com.github.redreaperlp.reaperutility.util.Embedder;
import com.github.redreaperlp.reaperutility.util.SmileyFormatter;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LCommand extends ListenerAdapter {

    @Override
    public void onGenericContextInteraction(@NotNull GenericContextInteractionEvent event) {
        ECommand command = ECommand.get(event.getName());
        UserStorage storage = UserManager.getUser(event.getUser());
        JGuild guild = JStorage.instance().getGuild(true,event.getGuild().getIdLong());
        JPermission permission = guild.getPermission(command.getName());
        if (!UserManager.isUserRateLimited(storage)) {
            if (command == ECommand.EVENT) {
                if (permission.hasPermission(event.getMember())) {
                    command.used(event.getUser());
                    PrivateChannel channel = event.getUser().openPrivateChannel().complete();
                    MessageChannelUnion union = (MessageChannelUnion) event.getMessageChannel();
                    Configurator configurator = new Configurator(channel, union);
                    Message message = channel.sendMessageEmbeds(Embedder.eventConfiguration(configurator))
                            .addComponents(Embedder.eventConfigurationActionRow(configurator)).complete();
                    configurator = new Configurator(message);
                    UserManager.getUser(event.getUser()).currentConfigurator(configurator);
                    event.replyEmbeds(Embedder.eventConfigurationSent(event.getUser(), message)).setEphemeral(true).complete();
                } else {
                    event.replyEmbeds(Embedder.noPermission(command.permission(), event.getGuild(), event.getUser())).setEphemeral(true).complete();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();
        UserStorage storage = UserManager.getUser(event.getUser());
        new Thread(() -> {
            if (!UserManager.isUserRateLimited(storage)) {
                ECommand command = ECommand.get(event.getName());
                Guild guild = null;
                JGuild jGuild = null;
                JPermission permission = null;
                boolean hasPermission = false;
                if (event.isFromGuild()) {
                    guild = event.getGuild();
                    event.getGuild();
                    jGuild = JStorage.instance().getGuild(true,guild.getIdLong());
                    permission = jGuild.getPermission(command.permission());
                    if (permission != null) {
                        hasPermission = permission.hasPermission(event.getMember());
                    }
                }
                switch (command) {
                    case EVENT -> {
                        if (hasPermission) {
                            command.used(user);
                            PrivateChannel channel = event.getUser().openPrivateChannel().complete();
                            Configurator configurator = new Configurator(channel, event.getChannel());
                            Message message = channel.sendMessageEmbeds(Embedder.eventConfiguration(configurator))
                                    .addComponents(Embedder.eventConfigurationActionRow(configurator)).complete();
                            configurator = new Configurator(message);
                            UserManager.getUser(event.getUser()).currentConfigurator(configurator);
                            event.replyEmbeds(Embedder.eventConfigurationSent(event.getUser(), message)).setEphemeral(true).complete();
                        } else {
                            event.replyEmbeds(Embedder.noPermission(command.permission(), guild, user)).setEphemeral(true).complete();
                        }
                    }
                    case CLEAR -> {
                        clear(event, storage, command, permission);
                    }
                    case SELECT_SEND_CHANNEL -> {
                        try {
                            command.used(user);
                            Configurator conf = storage.currentConfigurator();
                            if (conf != null) {
                                String channelID = event.getOption(ECommand.SELECT_SEND_CHANNEL.option(0)).getAsString();
                                GuildChannel channel = Main.jda.getGuildChannelById(channelID);
                                if (channel == null || !channel.getGuild().getId().equals(conf.guildChannel().getGuild().getId())) {
                                    event.reply("Channel not found").setEphemeral(true).queue();
                                    return;
                                }
                                if (channelID.equals(conf.guildChannel(true))) {
                                    event.reply("Channel already selected").setEphemeral(true).queue();
                                    return;
                                }
                                conf.guildChannel(channelID);
                                user.openPrivateChannel().complete().retrieveMessageById(conf.messageID().trim()).complete()
                                        .editMessageEmbeds(Embedder.eventConfiguration(conf)).queue();
                                event.reply("Channel selected: " + conf.guildChannel(false)).setEphemeral(true).queue();
                            } else {
                                event.replyEmbeds(Embedder.noConfiguratorAvailable()).addActionRow(LButton.EButton.HELP_EVENT_CREATE.getButton()).setEphemeral(true).queue();
                                new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.GREEN).append(" tried to select a channel without an event configuration", Color.GRAY).printWarning();
                            }
                        } catch (NumberFormatException e) {
                            event.replyEmbeds(Embedder.noConfiguratorAvailable()).addActionRow(LButton.EButton.HELP_EVENT_CREATE.getButton()).setEphemeral(true).queue();
                            new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.GREEN).append(" tried to select a channel without an event configuration", Color.GRAY).printWarning();
                        } catch (Exception e) {
                            e.printStackTrace();
                            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
                        }
                    }
                    case SELECT_ROLE_NOTIFY -> {
                        try {
                            command.used(user);
                            Configurator conf = storage.currentConfigurator();
                            String roleID = event.getOption(ECommand.SELECT_ROLE_NOTIFY.option(0)).getAsString();
                            Role role = Main.jda.getRoleById(roleID);
                            if (conf != null) {
                                if (role == null || !role.getGuild().getId().equals(conf.guildChannel().getGuild().getId())) {
                                    event.reply("Role not found").setEphemeral(true).queue();
                                    return;
                                }
                                conf.notification(role.getName());
                                user.openPrivateChannel().complete().retrieveMessageById(conf.messageID().trim()).complete()
                                        .editMessageEmbeds(Embedder.eventConfiguration(conf)).queue();
                                event.reply("Role selected: " + role.getName()).setEphemeral(true).queue();
                            } else {
                                event.replyEmbeds(Embedder.noConfiguratorAvailable()).addActionRow(LButton.EButton.HELP_EVENT_CREATE.getButton()).setEphemeral(true).queue();
                                new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.GREEN).append(" tried to select a role without an event configuration", Color.GRAY).printWarning();
                            }
                        } catch (NumberFormatException e) {
                            event.replyEmbeds(Embedder.noConfiguratorAvailable()).addActionRow(LButton.EButton.HELP_EVENT_CREATE.getButton()).setEphemeral(true).queue();
                            new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.RED).append(" tried to select a role without an event configuration", Color.GRAY).printWarning();
                        } catch (Exception e) {
                            e.printStackTrace();
                            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
                        }
                    }
                    case SET_PERMISSION -> {
                        Member member = event.getMember();
                        command.used(event.getUser());
                        if (!hasPermission) {
                            event.replyEmbeds(Embedder.noPermission(command.permission(), guild, user)).setEphemeral(true).queue();
                            return;
                        }
                        EPermission toSet = EPermission.get(event.getOption("command").getAsString());
                        if (toSet == EPermission.NONE) {
                            event.replyEmbeds(Embedder.notFound("Command"))
                                    .addActionRow(
                                            LButton.EButton.HELP_PERMISSIONS.getButton()
                                    ).setEphemeral(true).queue();
                            return;
                        }
                        Role role;
                        try {
                            role = guild.getRoleById(event.getOption("role").getAsString());
                        } catch (NumberFormatException e) {
                            event.replyEmbeds(Embedder.notFound("Role"))
                                    .addActionRow(
                                            LButton.EButton.HELP_PERMISSIONS.getButton()
                                    ).setEphemeral(true).queue();
                            return;
                        }
                        if (role == null) {
                            event.reply("Role not found").setEphemeral(true).queue();
                            return;
                        }
                        JPermission jPermission = jGuild.getPermission(toSet);
                        boolean added = jPermission.toggleRole(role, jGuild);
                        System.out.println("Added: " + added);
                        if (added) jGuild.addPermission(jPermission);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Permission set")
                                .setDescription("The permission " + toSet.name() + " was " + (added ? "added" : "removed") + " for the role " + role.getName())
                                .setAuthor(event.getUser().getAsTag(), "https://discord.gg/ghhKXDGQhD", event.getUser().getAvatarUrl())
                                .setThumbnail(guild.getIconUrl())
                                .setFooter("Powered by " + Main.jda.getSelfUser().getName(), Main.jda.getSelfUser().getAvatarUrl())
                                .setColor(0x00ff00)
                                .build()
                        ).setEphemeral(true).queue();
                    }
                    case NONE -> {
                        event.replyEmbeds(Embedder.unknownCommandEmbed()).setEphemeral(true).queue();
                    }
                }
            } else {
                storage.isNotified();
                event.replyEmbeds(Embedder.rateLimitEmbed(storage)).setEphemeral(true).queue();
            }
        }).start();
    }

    public void clear(GenericCommandInteractionEvent event, UserStorage storage, ECommand command, JPermission permission) {
        if (event.isFromGuild()) {
            if (!permission.hasPermission(event.getMember())) {
                event.replyEmbeds(Embedder.noPermission(command.permission(), event.getGuild(), event.getUser())).setEphemeral(true).complete();
                return;
            }
        }
        command.used(event.getUser());
        event.deferReply().setEphemeral(true).queue();
        int amount = event.getOption(ECommand.CLEAR.option(0)).getAsInt();
        if (amount > 100) {
            amount = 100;
        }
        if (!storage.clearIsActive()) {
            storage.clearActive();
            List<Message> messages = event.getMessageChannel().getHistory().retrievePast(amount).complete().stream()
                    .filter(message -> !event.isFromGuild() || message.getEmbeds().isEmpty() || message.getEmbeds().get(0).getTitle() == null || (!message.getEmbeds().get(0).getTitle().contains("Cleared")))
                    .filter(message -> {
                        if (!message.isFromGuild()) {
                            return message.getAuthor().isBot();
                        }
                        if (message.getComponents().isEmpty()) {
                            return true;
                        }
                        String id = message.getComponents().get(0).getButtons().get(0).getId();
                        return (id == null || !id.equals("accept-event"));
                    })
                    .toList();
            try {
                if (messages.size() != 0) {
                    List<CompletableFuture<Void>> future = event.getMessageChannel().purgeMessages(messages);
                    try {
                        while (future.size() != 0) {
                            future.removeIf(CompletableFuture::isDone);
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Cleared " + SmileyFormatter.numbersToSmileys(messages.size()) + " messages")
                            .setDescription("Successfully cleared " + SmileyFormatter.numbersToSmileys(messages.size()) + " messages")
                            .setColor(0x00ff00)
                            .build()
                    ).queue();
                } else {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Cleared 0️⃣ messages")
                            .setDescription("No messages to clear")
                            .setColor(0xffff00)
                            .build()
                    ).queue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            storage.clearInactive();
        } else {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Clearing already in progress")
                    .setDescription("Please wait until the current clearing process is finished")
                    .setColor(0xff0000)
                    .build()
            ).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        ECommand command = ECommand.get(event.getName());
        new Thread(() -> {
            Guild guild;
            switch (command) {
                case SELECT_SEND_CHANNEL:
                case SELECT_ROLE_NOTIFY:
                    UserStorage storage = UserManager.getUser(event.getUser());
                    if (storage.currentConfigurator() == null) {
                        List<Message> messages = event.getChannel().getHistory().retrievePast(3).complete().stream().filter(message -> {
                            if ((message.getEmbeds().size() > 0) && (message.getEmbeds().get(0).getTitle() != null)) {
                                return message.getEmbeds().get(0).getTitle().equals("Event Creation");
                            }
                            return false;
                        }).limit(1).toList();
                        if (messages.size() > 0) {
                            storage.currentConfigurator(new Configurator(messages.get(0)));
                        } else {
                            event.replyChoices(List.of()).queue();
                            return;
                        }
                    }
                    guild = storage.currentConfigurator().guildChannel().getGuild();
                    Configurator configurator = storage.currentConfigurator();
                    List<Command.Choice> choices;
                    if (command == ECommand.SELECT_SEND_CHANNEL) {
                        Member member = guild.getMember(event.getUser());
                        if (member == null) {
                            member = guild.retrieveMember(event.getUser()).complete();
                        }
                        String input = event.getOption(ECommand.SELECT_SEND_CHANNEL.option(0)).getAsString().toLowerCase();
                        Member finalMember = member;
                        choices = guild.getChannels().stream()
                                .filter(channel -> (finalMember.hasPermission(channel, Permission.VIEW_CHANNEL)) && !(channel instanceof Category) && (channel.getId().contains(input) || channel.getName().toLowerCase().contains(input) || input.isBlank()))
                                .map(channel -> new Command.Choice((configurator.hasGuildChannel(channel.getId()) ? "✅" : "❌") + ((channel.getType().isAudio() ? "\uD83D\uDD0A" : "\uD83D\uDCDD") + channel.getName()), channel.getId()))
                                .sorted(Comparator.comparing(Command.Choice::getName))
                                .limit(25)
                                .collect(Collectors.toList());
                    } else {
                        String input = event.getOption(ECommand.SELECT_ROLE_NOTIFY.option(0)).getAsString().toLowerCase();
                        choices = guild.getRoles().stream()
                                .filter(role -> role.getId().contains(input) || role.getName().toLowerCase().contains(input) || input.isBlank())
                                .map(role -> new Command.Choice((configurator.hasNotification(role.getName()) ? "✅" : "❌") + role.getName(), role.getId()))
                                .sorted(Comparator.comparing(Command.Choice::getName))
                                .limit(25)
                                .collect(Collectors.toList());

                    }
                    event.replyChoices(choices).queue();
                    break;
                case SET_PERMISSION:
                    JGuild jServer = JStorage.instance().getGuild(true,event.getGuild().getIdLong());
                    Member member = event.getMember();
                    if (member == null) member = event.getGuild().retrieveMember(event.getUser()).complete();
                    if (JStorage.instance().getGuild(true,event.getGuild().getIdLong()).getPermission(command.permission()).hasPermission(member)) {
                        guild = event.getGuild();
                        String input = event.getFocusedOption().getValue();
                        switch (event.getFocusedOption().getName()) {
                            case "command":
                                event.replyChoices(
                                        Arrays.stream(EPermission.values())
                                                .filter(permission -> (input.isBlank() || permission.get().contains(input)) && permission != EPermission.NONE)
                                                .map(command1 -> new Command.Choice(command1.get(), command1.get()))
                                                .sorted(Comparator.comparing(Command.Choice::getName))
                                                .limit(25)
                                                .collect(Collectors.toList())
                                ).queue();
                                break;
                            case "role":
                                EPermission permission = EPermission.get(event.getOption("command").getAsString());
                                event.replyChoices(
                                        guild.getRoles().stream()
                                                .filter(role -> input.isBlank() || role.getName().contains(input) || role.getId().contains(input))
                                                .map(role -> new Command.Choice((jServer.getPermission(permission).hasRole(role.getIdLong()) ? "✅" : "❌") + role.getName(), role.getId()))
                                                .sorted(Comparator.comparing(Command.Choice::getName))
                                                .limit(25)
                                                .collect(Collectors.toList())
                                ).complete();
                        }
                    } else {
                        event.replyChoices(List.of()).queue();
                    }
            }
        }).start();
    }
}
