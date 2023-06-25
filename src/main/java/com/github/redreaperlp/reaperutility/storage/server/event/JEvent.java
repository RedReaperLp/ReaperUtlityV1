package com.github.redreaperlp.reaperutility.storage.server.event;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class JEvent {
    private final long eventMessageID;
    private final long eventChannelID;
    private final long guildID;
    private long eventDate;

    transient private com.github.redreaperlp.reaperutility.storage.server.JGuild JGuild;

    public JEvent(JGuild jGuild, long eventMessageID, long eventChannelID, long guildID, long eventDate) {
        this.JGuild = jGuild;
        this.eventMessageID = eventMessageID;
        this.eventChannelID = eventChannelID;
        this.guildID = guildID;
        this.eventDate = eventDate;
    }

    public Configurator.FailReason setEventDate(String eventDate) {
        LocalDateTime localDateTime = LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Instant instant = localDateTime.toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
        long epochSecond = instant.getEpochSecond() - Instant.now().getEpochSecond();
        if (epochSecond > 15897567L) {
            return Configurator.FailReason.DATE_TO_FAR_IN_FUTURE;
        } else if (epochSecond < 0) {
            return Configurator.FailReason.DATE_IN_PAST;
        } else {
            this.eventDate = Instant.now().plusSeconds(epochSecond).getEpochSecond();
            return Configurator.FailReason.SUCCESS;
        }
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public long eventMessageID() {
        return eventMessageID;
    }

    public long eventChannelID() {
        return eventChannelID;
    }

    public long guildID() {
        return guildID;
    }

    public long eventDate() {
        return eventDate;
    }

    public void eventDate(long eventDate) {
        this.eventDate = eventDate;
        JStorage.instance().changes();
    }

    public JGuild JGuild() {
        return JGuild;
    }

    public long getRemainingTime() {
        return eventDate - Instant.now().getEpochSecond();
    }

    public JSONObject getWithInfos() {
        JSONObject jsonObject = new JSONObject();
        Message message;
        try {
            message = Main.jda.getTextChannelById(eventChannelID).retrieveMessageById(eventMessageID).complete();
        } catch (Exception e) {
            return null;
        }
        if (message != null) {
            MessageEmbed embed = message.getEmbeds().get(0);
            jsonObject.put("title", embed.getTitle());
            List<MessageEmbed.Field> fields = embed.getFields();
            for (MessageEmbed.Field field : fields) {
                jsonObject.put(field.getName().toLowerCase(Locale.ROOT), field.getValue().replaceFirst("> ", ""));
            }
        }
        return jsonObject;
    }
}
