package com.github.redreaperlp.reaperutility.events;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.scheduler.EventScheduler;
import com.github.redreaperlp.reaperutility.util.Color;
import com.github.redreaperlp.reaperutility.util.Embedder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configurator {
    private String name;
    private String description;
    private String location;
    private String date;
    private String notification;
    private String eventSendChannelID;
    private String messageID;
    private long channelID;
    private String remaining;

    private boolean editing = false;
    private long editMessageIDLong;


    public Configurator(String name, String description, String Location, String date, String notification, String remaining, String eventSendChannelID) {
        this.name = name;
        this.description = description;
        this.location = Location;
        this.date = date;
        this.notification = notification;
        this.remaining = remaining;
        this.eventSendChannelID = eventSendChannelID;
        this.editing = false;
    }

    public Configurator(PrivateChannel channel, Message message) {
        this.name = MarkdownUtil.quote("Name");
        this.description = MarkdownUtil.quote("Description");
        this.location = MarkdownUtil.quote("Location");
        this.date = MarkdownUtil.quote(Instant.now().plusSeconds(60).toString());
        this.notification = MarkdownUtil.quote("Notification");
        this.remaining = TimeFormat.RELATIVE.format(Instant.now().plusSeconds(60));
        this.eventSendChannelID = channel.getAsMention();
        this.messageID = message.getId();
        this.channelID = message.getChannel().getIdLong();
        this.editing = false;
    }

    public Configurator(PrivateChannel channel, MessageChannelUnion guildChannel) {
        this.name = "Name";
        this.description = "Description";
        this.location = "Location";
        this.date = TimeFormat.DEFAULT.format(Instant.now().plusSeconds(60));
        this.notification = "Notification";
        this.remaining = TimeFormat.RELATIVE.format(Instant.now().plusSeconds(60));
        this.eventSendChannelID = guildChannel.getAsMention();
        this.channelID = guildChannel.getIdLong();
        this.editing = false;
    }

    public Configurator(Message message) {
        MessageEmbed embed = message.getEmbeds().get(0);
        editing = embed.getTitle().equals("Event Editing");
        if (editing) {
            String[] messageLocation = embed.getUrl().replace("https://discord.com/channels/", "").split("/");
            guildChannel(messageLocation[1]);
            messageID = messageLocation[2];
        } else {
            this.messageID = message.getId();
        }
        List<MessageEmbed.Field> fields = embed.getFields();
        for (MessageEmbed.Field field : fields) {
            Embedder.EmbedField embedField = Embedder.EmbedField.get(field.getName());
            switch (embedField) {
                case NAME -> this.name = checkOrDefault(field.getValue(), embedField);
                case DESCRIPTION -> this.description = checkOrDefault(field.getValue(), embedField);
                case LOCATION -> this.location = checkOrDefault(field.getValue(), embedField);
                case DATE -> this.date = checkOrDefault(field.getValue(), embedField);
                case NOTIFICATION -> this.notification = checkOrDefault(field.getValue(), embedField);
                case REMAINING -> this.remaining = checkOrDefault(field.getValue(), embedField);
                case EVENT_SEND_CHANNEL -> this.eventSendChannelID = checkOrDefault(field.getValue(), embedField);
            }
        }
    }

    public Configurator(Message message, boolean editing) {
        this.editing = true;
        eventSendChannelID = message.getChannel().getId();
        editMessageIDLong = message.getIdLong();
        messageID = message.getId();
        List<MessageEmbed.Field> fields = message.getEmbeds().get(0).getFields();
        name = message.getEmbeds().get(0).getTitle();
        for (MessageEmbed.Field field : fields) {
            Embedder.EmbedField embedField = Embedder.EmbedField.get(field.getName());
            switch (embedField) {
                case DESCRIPTION -> this.description = checkOrDefault(field.getValue(), embedField);
                case LOCATION -> this.location = checkOrDefault(field.getValue(), embedField);
                case DATE -> this.date = checkOrDefault(field.getValue(), embedField);
                case REMAINING -> this.remaining = checkOrDefault(field.getValue(), embedField);
            }
        }
    }

    public String checkOrDefault(String toCeck, Embedder.EmbedField field) {
        if (toCeck == null) {
            switch (field) {
                case NAME -> toCeck = "Name";
                case DESCRIPTION -> toCeck = "Description";
                case LOCATION -> toCeck = "Location";
                case DATE -> toCeck = MarkdownUtil.quote(TimeFormat.DEFAULT.format(Instant.now().plusSeconds(60)));
                case NOTIFICATION -> toCeck = "Notification";
                case REMAINING -> toCeck = TimeFormat.RELATIVE.format(Instant.now().plusSeconds(60));
                case EVENT_SEND_CHANNEL -> toCeck = "Event Send Channel";
            }
            return toCeck;
        } else {
            return toCeck;
        }
    }

    private String remQ(String string) {
        return string.replaceFirst("> ", "");
    }

    public String name(boolean removeQuote) {
        String name = checkOrDefault(this.name, Embedder.EmbedField.NAME);
        if (removeQuote) {
            return remQ(name);
        } else {
            return name;
        }
    }

    public void name(String name) {
        this.name = MarkdownUtil.quote(preventBlank(name));
    }

    public String description(boolean removeQuote) {
        String description = checkOrDefault(this.description, Embedder.EmbedField.DESCRIPTION);
        if (removeQuote) {
            return remQ(description);
        } else {
            return description;
        }
    }

    public void description(String description) {
        if (description.equalsIgnoreCase("none")) {
            this.description = "Description";
        } else {
            this.description = MarkdownUtil.quote(preventBlank(description));
        }
    }

    public String location(boolean removeQuote) {
        String location = checkOrDefault(this.location, Embedder.EmbedField.LOCATION);
        if (removeQuote) {
            return remQ(location);
        } else {
            return location;
        }
    }

    public void location(String location) {
        if (location.equalsIgnoreCase("none")) {
            this.location = "Location";
        } else {
            this.location = MarkdownUtil.quote(preventBlank(location));
        }
    }

    public String date() {
        return remQ(checkOrDefault(date, Embedder.EmbedField.DATE));
    }

    public FailReason date(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Instant instant = localDateTime.toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
        this.date = MarkdownUtil.quote(TimeFormat.DEFAULT.format(instant.atOffset(ZoneOffset.UTC)));
        if (remainingSeconds() > 15897567L) {
            return FailReason.DATE_TO_FAR_IN_FUTURE;
        } else if (remainingSeconds() < 0) {
            return FailReason.DATE_IN_PAST;
        } else {
            return FailReason.SUCCESS;
        }
    }

    public String notification() {
        return checkOrDefault(notification, Embedder.EmbedField.NOTIFICATION);
    }

    public void notification(String notification) {
        List<String> split = new java.util.ArrayList<>(Arrays.stream(this.notification.replaceAll("[ >]", "").split("\n")).toList());
        if (split.contains(notification)) {
            if (split.contains("@everyone")) {
                split.clear();
                split.add(notification);
            }
            split.remove(notification);
            if (split.isEmpty()) {
                split.add("Notification");
            }
        } else {
            if (notification.equals("@everyone")) {
                split.clear();
                split.add(notification);
            } else if (split.contains("Notification")) {
                split.remove("Notification");
                split.add(notification);
            } else {
                if (split.contains("@everyone")) {
                    split.clear();
                }
                split.add(notification);
            }
        }
        String newNotification = String.join("\n", split);
        this.notification = MarkdownUtil.quote(newNotification);
    }

    public boolean hasNotification(String roleName) {
        return notification().contains(roleName) || notification().contains("@everyone");
    }

    public long remainingSeconds() {
        return (Long.parseLong(date.split(":")[1]) - Instant.now().getEpochSecond());
    }

    public String remaining() {
        String remaining;
        try {
            remaining = TimeFormat.RELATIVE.format(Instant.ofEpochSecond(Long.parseLong(date.split(":")[1])));
        } catch (Exception e) {
            remaining = "Time is not valid";
        }
        return remaining;
    }

    public void messageID(Message message) {
        messageID = message.getId();
    }

    public String messageID() {
        return messageID;
    }

    public String guildChannel(boolean filterMarkdown) {
        String id = checkOrDefault(eventSendChannelID, Embedder.EmbedField.EVENT_SEND_CHANNEL);
        if (filterMarkdown) {
            return id.replaceAll("[<#>]", "").trim();
        }
        return id.trim();
    }

    public boolean hasGuildChannel(String channelID) {
        return guildChannel(true).equals(channelID);
    }

    public void guildChannel(String eventsToSend) {
        this.eventSendChannelID = MarkdownUtil.quote("<#" + eventsToSend + ">");
    }

    public GuildChannel guildChannel() {
        return Main.jda.getGuildChannelById(guildChannel(true).trim());
    }

    public String preventBlank(String toCheck) {
        List<String> lines = new ArrayList<>(Arrays.asList(toCheck.split("\n")));
        List<String> nonBlankLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.replace(">", "").isBlank()) {
                nonBlankLines.add(line);
            } else {
                nonBlankLines.add(" ");
            }
        }
        nonBlankLines.forEach(Color::printTest);
        return String.join("\n", nonBlankLines);
    }

    public EventScheduler.EventStorage getEvent() {
        return new EventScheduler.EventStorage(name, date, channelID);
    }

    public FailReason isValid() {
        if (remainingSeconds() > 15897567L) {
            return FailReason.DATE_TO_FAR_IN_FUTURE;
        } else if (remainingSeconds() < 0) {
            return FailReason.DATE_IN_PAST;
        } else if (!name.contains(">")) {
            return FailReason.NO_NAME;
        } else {
            return FailReason.SUCCESS;
        }
    }

    public boolean isEditing() {
        return editing;
    }

    public enum FailReason {
        DATE_TO_FAR_IN_FUTURE("Date is to far in the future"),
        DATE_IN_PAST("Date is in the past"),
        DATE_NOT_VALID("Date is not valid"),
        NO_NAME("Name is not valid"),
        SUCCESS("Success"),
        NOT_PART_OF_GUILD("You are not part of the guild"),
        NONE("none");

        String reason;

        FailReason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}
