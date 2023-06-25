package com.github.redreaperlp.reaperutility.command.handler;

import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.util.Embedder;
import com.github.redreaperlp.reaperutility.user.UserManager;
import com.github.redreaperlp.reaperutility.user.UserStorage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LModal extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        UserStorage storage = UserManager.getUser(event.getUser());
        ModalType type = ModalType.get(event.getInteraction().getModalId());
        if (type == ModalType.NONE) return;
        Message message = event.getMessage();
        new Thread(() -> {
            Configurator configurator = storage.currentConfigurator();
            switch (type) {
                case EVENT_NAME:
                case EVENT_DESCRIPTION:
                    if (configurator == null) {
                        configurator = new Configurator(message);
                    }
                    if (type == ModalType.EVENT_NAME) {
                        String name = event.getValues().get(0).getAsString();
                        configurator.name(name);
                        event.editComponents(Embedder.eventConfigurationActionRow(configurator)).queue();
                    } else {
                        String description = event.getValues().get(0).getAsString();
                        configurator.description(description);
                        event.editComponents(Embedder.eventConfigurationActionRow(configurator)).queue();
                    }
                    event.getMessage().editMessageEmbeds(Embedder.eventConfiguration(configurator)).queue();
                    break;
                case EVENT_DATE:
                    String[] dates = event.getValues().get(0).getAsString().replaceAll("[\n]", "").split(" ");
                    if (dates.length == 2) {
                        String[] date1 = dates[0].split("-");
                        String[] date2 = dates[1].split(":");
                        if (date1.length != 3 || date2.length != 2) {
                            event.reply("The date must be in the format yyyy-MM-dd HH:mm").setEphemeral(true).queue();
                            return;
                        } else {
                            try {
                                String date = dates[0] + " " + dates[1];
                                if (configurator == null) {
                                    configurator = new Configurator(event.getMessage());
                                }
                                Configurator.FailReason reason = configurator.date(date);
                                if (reason == Configurator.FailReason.SUCCESS) {
                                    event.editMessageEmbeds(Embedder.eventConfiguration(configurator)).queue();
                                    message.editMessageComponents(Embedder.eventConfigurationActionRow(configurator)).queue();
                                } else {
                                    event.getMessage().editMessageComponents(Embedder.eventConfigurationActionRow(configurator)).queue();
                                    event.replyEmbeds(Embedder.failReason(reason, event.getUser())).setEphemeral(true).queue();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                event.reply("The date must be in the format yyyy-MM-dd HH:mm").setEphemeral(true).queue();
                                return;
                            }
                        }
                    } else {
                        event.reply("The date must be in the format yyyy-MM-dd HH:mm").setEphemeral(true).queue();
                        return;
                    }
                    break;
                case EVENT_LOCATION:
                    String location = event.getValues().get(0).getAsString();
                    if (configurator == null) {
                        configurator = new Configurator(event.getMessage());
                    }
                    GuildChannel channel = configurator.guildChannel();
                    Guild guild = channel.getGuild();
                    Optional<GuildChannel> first = guild.getChannels().stream().filter(c -> (c.getName().contains(location.split("\n")[0].trim()) || c.getId().contains(location.split("\n")[0].trim())) && (c.getType().isMessage() || c.getType().isAudio())).findFirst();
                    if (first.isPresent()) {
                        configurator.location(first.get().getAsMention());
                    } else {
                        configurator.location(location);
                    }
                    event.editMessageEmbeds(Embedder.eventConfiguration(configurator)).queue();
                    break;
            }
        }).start();
    }

    public enum ModalType {
        EVENT_NAME("event-name", "here you can enter the name of the event, you can use discord markdown", "Event Name1️⃣"),
        EVENT_DESCRIPTION("event-description", "here you can enter the description of the event", "Event Description2️⃣"),
        EVENT_DATE("event-date", "the date of the event in the format yyyy-MM-dd HH:mm", "Event Date3️⃣", TextInputStyle.SHORT),
        EVENT_LOCATION("event-location", "here you can enter the location of the event for example in a voice channel or in a text channel", "Event Location4️⃣"),
        NONE("none", "none", "none");

        private final String name;
        private final String placeholder;
        private final String title;
        private final TextInputStyle style;

        ModalType(String name, String placeholder, String title) {
            this.name = name;
            this.placeholder = placeholder;
            this.title = title;
            this.style = TextInputStyle.PARAGRAPH;
        }

        ModalType(String name, String placeholder, String title, TextInputStyle style) {
            this.name = name;
            this.placeholder = placeholder;
            this.title = title;
            this.style = style;
        }

        public String cName() {
            return name;
        }

        public String placeholder() {
            return placeholder;
        }

        public static ModalType get(String name) {
            for (ModalType type : values()) {
                if (type.name.equals(name)) return type;
            }
            return NONE;
        }

        public Modal getModal() {
            return Modal.create(name, title).addActionRow(
                    TextInput.create(name, "Enter the " + name, style).setPlaceholder(placeholder).build()
            ).build();
        }


        public Modal getModal(String value) {
            return Modal.create(name, title).addActionRow(
                    TextInput.create(name, "Enter the " + name, style).setPlaceholder(placeholder).setValue(value).build()
            ).build();
        }
    }
}
