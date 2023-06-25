package com.github.redreaperlp.reaperutility.enums;

import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ECommand {
    EVENT(true, "event-new", "this command is used to create and schedule events", EPermission.EVENT_CREATE),
    SELECT_SEND_CHANNEL(false, "select-send-channel", "select the channel where the event should be sent", EPermission.NONE,
            new CommandOption(OptionType.STRING, "channel", "the channel where the event should be sent", true, true)),
    SELECT_ROLE_NOTIFY(false, "select-role-notify", "select the role(s) which should be notified", EPermission.NONE,
            new CommandOption(OptionType.STRING, "role", "the role which should be notified", true, true)),
    SET_PERMISSION(true, "set-permission", "set the permission for a command", EPermission.SET_PERMISSION,
            new CommandOption(OptionType.STRING, "command", "the command the permission should be set for", true, true),
            new CommandOption(OptionType.STRING, "role", "the role which should be able to use the command (this toggles)", true, true)),
    CLEAR(true, "clear", "clears the last x messages", EPermission.CLEAR,
            new CommandOption(OptionType.INTEGER, "amount", "the amount of messages which should be cleared", true, false)),
    NONE(false, "none", "", EPermission.NONE),
    ;

    private final String name;
    private final boolean isInGuild;
    private final String description;
    private EPermission permission;
    List<CommandOption> options;

    ECommand(boolean isInGuild, String name, String description, EPermission permission) {
        this.isInGuild = isInGuild;
        this.name = name;
        this.description = description;
        this.permission = permission;
    }

    ECommand(String name, String description, EPermission permission) {
        this.isInGuild = false;
        this.name = name;
        this.description = description;
        this.permission = permission;
    }

    ECommand(boolean isInGuild, String name, String description, List<CommandOption> options) {
        this.isInGuild = isInGuild;
        this.name = name;
        this.description = description;
        this.options = options;
    }

    ECommand(boolean isInGuild, String name, String description, EPermission permission, CommandOption... options) {
        this.isInGuild = isInGuild;
        List<CommandOption> list = new ArrayList<>();
        Collections.addAll(list, options);
        this.name = name;
        this.description = description;
        this.options = list;
        this.permission = permission;
    }

    public SlashCommandData prepareSlash() {
        SlashCommandData command = Commands.slash(name, description);
        if (options == null) return command;
        for (CommandOption option : options) {
            command.addOption(option.type(), option.name(), option.description(), option.required(), option.autocomplete());
        }
        return command;
    }

    public CommandData prepareContext() {
        return Commands.context(Command.Type.USER, name);
    }

    public CommandData prepareMessage() {
        return Commands.message(name);
    }

    public static ECommand get(String name) {
        for (ECommand command : values()) {
            if (command.name.equals(name)) {
                return command;
            }
        }
        return NONE;
    }

    public String option(int i) {
        return options.get(i).name();
    }

    public CommandOption option(String name) {
        for (CommandOption option : options) {
            if (option.name().equals(name)) {
                return option;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void used(User user) {
        new Color.Print("User ", Color.GRAY).append(user.getAsTag(), Color.GREEN).append(" used command ", Color.GRAY).append(name, Color.GREEN).printInfo();
    }

    public EPermission permission() {
        return permission;
    }

    public boolean isInGuild() {
        return isInGuild;
    }

    public static class CommandOption extends Commands {
        private final OptionType type;
        private final String name;
        private final String description;
        private boolean required;
        private boolean autocomplete;

        public CommandOption(OptionType type, String name, String description) {
            this.type = type;
            this.name = name;
            this.description = description;
        }

        public CommandOption(OptionType type, String name, String description, boolean required, boolean autocomplete) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.required = required;
            this.autocomplete = autocomplete;
        }

        public OptionType type() {
            return type;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public boolean required() {
            return required;
        }

        public boolean autocomplete() {
            return autocomplete;
        }

    }
}
