package com.github.redreaperlp.reaperutility.scheduler;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.command.handler.LButton;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.util.Embedder;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.event.JEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EventScheduler implements Runnable {
    private static final List<JEvent> events = new ArrayList<>();
    private static Thread thread;

    public static Message schedule(EventStorage storage, String notify) {
        if (!storage.isEditing()) {
            GuildMessageChannel textChannel = (GuildMessageChannel) Main.jda.getGuildChannelById(storage.eventSendChannelID(true));
            JGuild guild = JStorage.instance().getGuild(true, textChannel.getGuild().getIdLong());
            String notifyString = Arrays.stream(notify.replaceAll(">", "").split("\n"))
                    .flatMap(s -> Main.jda.getRolesByName(s.trim(), true).stream())
                    .map(Role::getAsMention)
                    .distinct()
                    .collect(Collectors.joining("\n"));
            Message message = textChannel.sendMessage(notifyString).addEmbeds(Embedder.eventEmbed(storage)).addComponents(
                    ActionRow.of(
                            LButton.EButton.ACCEPT_EVENT.getButton(),
                            LButton.EButton.UNSURE_EVENT.getButton(),
                            LButton.EButton.DECLINE_EVENT.getButton()),
                    ActionRow.of(
                            LButton.EButton.DELETE_EVENT.getButton(),
                            LButton.EButton.MODIFY_EVENT.getButton()
                    )).complete();
            JEvent event = new JEvent(guild, message.getIdLong(), textChannel.getIdLong(), textChannel.getGuild().getIdLong(), Long.parseLong(storage.date(true)));
            guild.addEvent(event);
            events.add(event);
            if (thread == null) {
                thread = new Thread(new EventScheduler());
                thread.start();
            } else if (!thread.isAlive()) {
                thread = new Thread(new EventScheduler());
                thread.start();
            }
            return message;
        } else {
            GuildMessageChannel channel = Main.jda.getTextChannelById(storage.channelID);
            Message message = channel.retrieveMessageById(storage.messageID).complete();
            JGuild guild = JStorage.instance().getGuild(true, channel.getGuild().getIdLong());
            JEvent event = guild.getEvent(Long.parseLong(storage.messageID));
            event.setEventDate(Long.parseLong(storage.date(true)));
            message.editMessageEmbeds(
                    Embedder.eventEmbed(new EventStorage(message).edit(storage))
            ).queue();
            return message;
        }
    }

    public static void schedule(JEvent event) {
        JEvent change = null;
        for (JEvent e : events) {
            if (e.eventMessageID() == event.eventMessageID()) {
                change = e;
                break;
            }
        }
        if (change != null) {
            GuildMessageChannel channel = Main.jda.getTextChannelById(event.eventChannelID());
            Message message = channel.retrieveMessageById(event.eventMessageID()).complete();
            message.editMessageEmbeds(
                    Embedder.eventEmbed(new EventStorage(message, true).date(event.eventDate()))
            ).queue();
            events.remove(change);
            JStorage.instance().changes();
        }
        events.add(event);
        if (thread == null) {
            thread = new Thread(new EventScheduler());
            thread.start();
        } else if (!thread.isAlive()) {
            thread = new Thread(new EventScheduler());
            thread.start();
        }
    }

    public static void removeEvent(Message eventMessageID) {
        JGuild server = JStorage.instance().getGuild(true, eventMessageID.getGuild().getIdLong());
        server.removeEvent(eventMessageID.getIdLong());
    }

    @Override
    public void run() {
        new Color.Print("Starting EventScheduler", Color.GREEN).printInfo();
        while (!events.isEmpty()) {
            try {
                List<JEvent> toRemove = new ArrayList<>();
                for (JEvent event : events) {
                    if (event.getRemainingTime() <= 0) {
                        GuildMessageChannel channel = Main.jda.getTextChannelById(event.eventChannelID());
                        JGuild server = JStorage.instance().getGuild(true, channel.getGuild().getIdLong());
                        if (fire(event, server)) {
                            toRemove.add(event);
                        }
                    }
                }
                events.removeAll(toRemove);
                for (JEvent event : toRemove) {
                    Color.printTest("Event " + event.eventMessageID() + " fired", Color.GREEN);
                    JGuild server = JStorage.instance().getGuild(true, event.guildID());
                    server.removeEvent(event.eventMessageID());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        new Color.Print("Stopping EventScheduler", Color.ORANGE).printInfo();
    }

    /**
     * Fires an event
     * @param event the event to fire
     * @param server the server the event is in
     * @return true if the event was fired, false if the event was deleted
     */
    private boolean fire(JEvent event, JGuild server) {
        GuildMessageChannel channel = Main.jda.getTextChannelById(event.eventChannelID());
        Message message = channel.retrieveMessageById(event.eventMessageID()).complete();
        if (message == null) {
            server.removeEvent(event.eventMessageID());
            return false;
        }
        EventStorage storage = new EventStorage(message, true);
        MessageEmbed embed = Embedder.notifyUsers(storage, message.getJumpUrl());
        Color.LIGHT_BLUE.printInfo("Firering event: " + storage.name + " at " + storage.channelID);
        for (String userString : storage.accepted) {
            if (userString.equals("-")) continue;
            User user = Main.jda.retrieveUserById(userString.replaceAll("[<@>]", "")).complete();
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embed).queue());
            new Color.Print(" - Sending Reminder to " + user.getAsTag(), Color.LIGHT_BLUE).printInfo();
        }
        for (String userString : storage.unsure) {
            if (userString.equals("-")) continue;
            User user = Main.jda.retrieveUserById(userString.replaceAll("[<@>]", "")).complete();
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embed).queue());
            new Color.Print(" - Sending Reminder to " + user.getAsTag(), Color.LIGHT_BLUE).printInfo();
        }
        return true;
    }

    public static class EventStorage {

        private String name;
        private String description;
        private String location;
        private String date;
        private String eventSendChannelID;
        private long channelID;
        private String messageID;

        private boolean editing;

        List<String> accepted = new ArrayList<>();
        List<String> declined = new ArrayList<>();
        List<String> unsure = new ArrayList<>();

        public EventStorage(String name, String date, long channelID) {
            this.name = name;
            this.date = date;
            this.channelID = channelID;
        }

        public EventStorage(Message message) {
            this(message, false);
        }

        public EventStorage(Message message, boolean isEvent) {
            channelID = message.getChannel().getIdLong();
            messageID = message.getId();
            MessageEmbed embed = message.getEmbeds().get(0);
            editing = embed.getTitle().equals("Event Editing");
            if (editing) {
                String[] messageLocation = embed.getUrl().replace("https://discord.com/channels/", "").split("/");
                channelID = Long.parseLong(messageLocation[1]);
                messageID = messageLocation[2];
            }
            List<MessageEmbed.Field> fields = embed.getFields();
            if (isEvent) name = message.getEmbeds().get(0).getTitle();
            for (MessageEmbed.Field field : fields) {
                switch (Embedder.EmbedField.get(field.getName())) {
                    case NAME:
                        name = field.getValue().replace("> ", "");
                        break;
                    case DESCRIPTION:
                        description = field.getValue();
                        break;
                    case DATE:
                        this.date = field.getValue();
                        break;
                    case LOCATION:
                        location = field.getValue();
                        break;
                    case EVENT_SEND_CHANNEL:
                        eventSendChannelID = field.getValue();
                        break;
                    case EVENT_ACCEPTED:
                        accepted = new ArrayList<>(Arrays.asList(field.getValue().split("\n")));
                        break;
                    case EVENT_DECLINED:
                        declined = new ArrayList<>(Arrays.asList(field.getValue().split("\n")));
                        break;
                    case EVENT_UNSURE:
                        unsure = new ArrayList<>(Arrays.asList(field.getValue().split("\n")));
                        break;
                }
            }
            if (description != null && !description.contains(">")) {
                description = null;
            }
            if (location != null && !location.contains(">")) {
                location = null;
            }
            if (accepted.size() == 0) {
                accepted.add("-");
            }
            if (declined.size() == 0) {
                declined.add("-");
            }
            if (unsure.size() == 0) {
                unsure.add("-");
            }
        }

        public EventStorage edit(EventStorage storage) {
            this.name = storage.name;
            this.description = storage.description;
            this.location = storage.location;
            this.date = storage.date;
            this.channelID = storage.channelID;
            this.messageID = storage.messageID;
            return this;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public String date(boolean replaceFormat) {
            if (replaceFormat) {
                return date.replaceAll("[<:ft>]", "").trim();
            }
            return date;
        }

        public EventStorage date(long eventDate) {
            this.date = "<t:" + eventDate + ":f>";
            return this;
        }

        public long remainingSeconds() {
            return (Long.parseLong(date.split(":")[1]) - Instant.now().getEpochSecond());
        }

        public String location() {
            return location;
        }

        public String messageID() {
            return messageID;
        }

        public long channelID() {
            return channelID;
        }

        public String eventSendChannelID() {
            return eventSendChannelID;
        }

        public String eventSendChannelID(boolean replaceFormat) {
            return eventSendChannelID.replaceAll("[<#>]", "").trim();
        }

        public void setMessageID(String messageID) {
            this.messageID = messageID;
        }

        public boolean addAccepted(User user) {
            return addStatus(user.getAsMention(), accepted, declined, unsure);
        }

        public boolean addDeclined(User user) {
            return addStatus(user.getAsMention(), declined, accepted, unsure);
        }

        public boolean addUnsure(User user) {
            return addStatus(user.getAsMention(), unsure, accepted, declined);
        }

        private boolean addStatus(String userID, List<String> targetList, List<String> otherList1, List<String> otherList2) {
            if (targetList.contains(userID)) return false;
            targetList.add(userID);
            otherList1.remove(userID);
            otherList2.remove(userID);
            targetList.remove("-");
            if (!otherList1.contains("-") && !containsSubstring(otherList1, "<@")) {
                otherList1.add("-");
            }
            if (!otherList2.contains("-") && !containsSubstring(otherList2, "<@")) {
                otherList2.add("-");
            }
            return true;
        }

        public List<String> getAccepted() {
            return accepted;
        }

        public List<String> getDeclined() {
            return declined;
        }

        public List<String> getUnsure() {
            return unsure;
        }

        public boolean containsSubstring(List<String> list, String substring) {
            for (String s : list) {
                if (s.contains(substring)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isEditing() {
            return editing;
        }
    }
}
