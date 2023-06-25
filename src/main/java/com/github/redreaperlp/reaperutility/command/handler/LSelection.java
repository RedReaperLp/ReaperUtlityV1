package com.github.redreaperlp.reaperutility.command.handler;

import com.github.redreaperlp.reaperutility.events.Configurator;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LSelection extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        ESelectMenu menu = ESelectMenu.get(event.getComponentId());
        if (Objects.requireNonNull(menu) == ESelectMenu.EVENT_CREATION) {
            SelectOption option = event.getInteraction().getSelectedOptions().get(0);
            ESelectOptions selectOption = ESelectOptions.get(option.getValue());
            LModal.ModalType type = LModal.ModalType.NONE;
            boolean modal = false;
            Configurator configurator = new Configurator(event.getMessage());
            switch (selectOption) {
                case EVENT_NAME:
                    type = LModal.ModalType.EVENT_NAME;
                    modal = true;
                    break;
                case EVENT_DESCRIPTION:
                    type = LModal.ModalType.EVENT_DESCRIPTION;
                    modal = true;
                    break;
                case EVENT_DATE:
                    type = LModal.ModalType.EVENT_DATE;
                    event.replyModal(
                            type.getModal(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    ).queue();
                    return;
                case EVENT_LOCATION:
                    type = LModal.ModalType.EVENT_LOCATION;
                    modal = true;
                    break;
            }
            if (modal) {
                if (type != LModal.ModalType.NONE) {
                    event.replyModal(
                            type.getModal()
                    ).queue();
                } else {
                    event.reply("This option is not implemented yet").setEphemeral(true).queue();
                }
            }
        }
    }

    public enum ESelectMenu {
        EVENT_CREATION("Event Creation", "event-creation"),
        EVENT_CREATION_NOTIFICATION("Event Creation Notification", "event-creation-notification"),
        EVENT_CREATION_CHANNEL("Event Creation Channel", "event-creation-channel"),
        NONE("None", "none");

        private final String label;
        private final String name;

        ESelectMenu(String label, String name) {
            this.label = label;
            this.name = name;
        }

        public String label() {
            return label;
        }

        public String cName() {
            return name;
        }

        public static ESelectMenu get(String name) {
            for (ESelectMenu menu : values()) {
                if (menu.name.equals(name)) return menu;
            }
            return NONE;
        }

        public SelectOption option() {
            return SelectOption.of(label, name);
        }
    }

    public enum ESelectOptions {
        EVENT_NAME("Event Name1️⃣", "event-name"),
        EVENT_DESCRIPTION("Event Description2️⃣", "event-description"),
        EVENT_DATE("Event Date3️⃣", "event-date"),
        EVENT_LOCATION("Event Location4️⃣", "event-location"),
        EVENT_NOTIFICATION("Event Notification5️⃣", "event-notification"),
        EVENT_SEND_CHANNEL("Event Send Channel6️⃣", "event-send-channel"),
        ;

        private final String label;
        private final String name;

        ESelectOptions(String label, String name) {
            this.label = label;
            this.name = name;
        }

        public String label() {
            return label;
        }

        public String cName() {
            return name;
        }

        public static ESelectOptions get(String name) {
            for (ESelectOptions option : values()) {
                if (option.name.equals(name)) return option;
            }
            return null;
        }

        public SelectOption option() {
            return SelectOption.of(label, name);
        }
    }
}
