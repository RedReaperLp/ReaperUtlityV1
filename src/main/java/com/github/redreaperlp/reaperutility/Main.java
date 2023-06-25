package com.github.redreaperlp.reaperutility;

import ch.qos.logback.classic.Logger;
import com.github.redreaperlp.reaperutility.api.Receiver;
import com.github.redreaperlp.reaperutility.command.handler.*;
import com.github.redreaperlp.reaperutility.config.Configuration;
import com.github.redreaperlp.reaperutility.config.console.JConsoleSettings;
import com.github.redreaperlp.reaperutility.config.jda.JDASettings;
import com.github.redreaperlp.reaperutility.enums.ECommand;
import com.github.redreaperlp.reaperutility.guild.LBotGuildLeaveJoin;
import com.github.redreaperlp.reaperutility.scheduler.EventScheduler;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.storage.server.event.JEvent;
import com.github.redreaperlp.reaperutility.util.Color;
import com.github.redreaperlp.reaperutility.util.ColorLoggerFactory;
import com.github.redreaperlp.reaperutility.util.Restarter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static JDA jda;
    public static boolean debug = false;
    public static Configuration conf;
    public static boolean colored;

    public static void main(String[] args) throws InterruptedException {
//        TimeUnit.SECONDS.sleep(5);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                Color.RED.printError("Exception in thread " + t.getName() + ": " + e.getMessage());
                String stackTrace = "";
                for (StackTraceElement str : e.getStackTrace()) {
                    stackTrace += str.toString() + "\n";
                }
                jda.getTextChannelById(1086673451777007636L).sendMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("Exception in thread " + t.getName() + ": " + e.getMessage())
                                .setDescription("```" + stackTrace + "```")
                                .build()
                ).queue();
            }
        });


        Main main = new Main().prepareConfig();
        new Thread(() -> {
            Receiver receiver = new Receiver();
            Thread thread = new Thread(receiver);
            thread.start();
            while (true) {
                if (!thread.isAlive()) {
                    receiver.close();
                    thread = new Thread(receiver);
                    thread.start();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(new Restarter()).start();
        if (conf.consoleSettings() == null) {
            conf.consoleSettings(new JConsoleSettings());
        }
        colored = conf.consoleSettings().coloredConsole();
        if (args.length > 0) {
            if (args[0].equals("debug")) {
                debug = true;
                Color.RED.printSuccess("Debug mode enabled!");
            }
        }
        main.start();
    }


    public void start() {
        Logger logger = (Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        logger.getLoggerContext().reset();
        logger.addAppender(new ColorLoggerFactory());
        logger.getAppender("ColorLogger").start();

        checkConsole();
        prepareStorage();
        JDASettings sets = conf.jdaSettings();
        JDABuilder jdaBuilder = JDABuilder.createDefault(sets.token());
        enableIntents(jdaBuilder);
        jdaBuilder.setEnableShutdownHook(true);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setActivity(Activity.of(sets.activity(), sets.activityText(), "twitch.tv/redreaperlp"));
        try {
            new Color.Print("Trying to connect to Discord...", Color.LIGHT_BLUE).printInfo();
            jda = jdaBuilder.build();
            new Color.Print("Connected to Discord!", Color.GREEN).printInfo();
        } catch (InvalidTokenException e) {
            new Color.Print("Invalid Token!", Color.RED)
                    .addLine("Please check your token in the config.json!", Color.RED).printError();
            System.exit(0);
        }
        try {
            jda.awaitReady();
            jda.updateCommands().addCommands(
                    prepareCommands(List.of(
                            ECommand.CLEAR,
                            ECommand.SELECT_ROLE_NOTIFY,
                            ECommand.SELECT_SEND_CHANNEL
                    ))
            ).complete();
            new Color.Print("INVITE: " + jda.getInviteUrl(Permission.ADMINISTRATOR)).printInfo();
            jda.addEventListener(new LCommand(), new LButton(), new LModal(), new LContext(), new LSelection(), new LBotGuildLeaveJoin());
            jda.getGuilds().forEach(guild -> {
                JStorage.instance().getGuild(true, guild.getIdLong());
                prepareCommands(guild);
                new Color.Print("Loaded commands for ", Color.GREEN).append(guild.getName(), Color.YELLOW).append(" with ", Color.GREEN).append(guild.getMemberCount() + " members", Color.YELLOW).printInfo();
            });
            for (JGuild server : JStorage.instance().getGuilds()) {
                if (server.getEvents() != null) {
                    for (JEvent event : server.getEvents()) {
                        EventScheduler.schedule(event);
                    }
                }
            }
            Color.GREEN.printInfo("Bot is ready!");
            save();
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }


    private void prepareStorage() {
        new JStorage(true);
    }

    private Main prepareConfig() {
        try {
            File file = new File("config.json");
            if (!file.exists()) {
                file.createNewFile();
                Configuration conf = new Configuration();
                conf.save();
            } else {
                Scanner scanner = new Scanner(file);
                String content = "";
                while (scanner.hasNextLine()) {
                    content += scanner.nextLine();
                }
                conf = new Gson().fromJson(content, Configuration.class);
                if (conf == null) {
                    conf = new Configuration();
                    conf.save();
                }
            }
            return this;
        } catch (FileNotFoundException e) {
            new Color.Print("Config file not found!", Color.RED).printError();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void enableIntents(JDABuilder build) {
        build.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        build.enableIntents(GatewayIntent.GUILD_MEMBERS);
        build.enableIntents(GatewayIntent.GUILD_PRESENCES);
        build.enableIntents(GatewayIntent.GUILD_MESSAGES);
        build.enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        build.enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        build.enableIntents(GatewayIntent.DIRECT_MESSAGES);
        build.enableIntents(GatewayIntent.GUILD_VOICE_STATES);
        build.enableIntents(GatewayIntent.SCHEDULED_EVENTS);
    }

    public void checkConsole() {
        if (System.console() == null) {
            File file = new File("start.bat");
            if (file.exists()) {
                try {
                    Runtime.getRuntime().exec("cmd /c start start.bat");
                    System.exit(0);
                } catch (IOException e) {
                    new Color.Print("Could not start the bot!", Color.RED).printError();
                    throw new RuntimeException(e);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please start the bot from the console, use the generated .bat file or start the .jar again!", "Start.bat not found!", JOptionPane.ERROR_MESSAGE);
            }
            try {
                FileWriter writer = new FileWriter(file);
                writer.write("java -jar " + System.getProperty("sun.java.command") + " && exit");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }
    }

    public static void prepareCommands(Guild guild) {
        guild.updateCommands().addCommands(
                ECommand.EVENT.prepareSlash(),
                ECommand.EVENT.prepareContext(),
                ECommand.EVENT.prepareMessage(),
                ECommand.SET_PERMISSION.prepareSlash()
        ).complete();
    }

    private List<CommandData> prepareCommands(List<ECommand> selectSendChannel) {
        List<CommandData> commandDatas = new ArrayList<>();
        selectSendChannel.forEach(eCommand -> commandDatas.add(eCommand.prepareSlash()));
        return commandDatas;
    }

    public void save() {
        JStorage instance = JStorage.instance();
        File file = new File(Main.conf.files().serverFile());
        new Thread(() -> {
            while (true) {
                if (instance.getChanges()) {
                    try {
                        String json = new GsonBuilder().setPrettyPrinting().create().toJson(instance);
                        file.delete();
                        file.createNewFile();
                        FileWriter writer = new FileWriter(file);
                        writer.write(json);
                        writer.flush();
                        writer.close();
                        instance.setChanges(false);
                        new Color.Print("Saved changes to ", Color.CYAN).append(file.getName(), Color.ORANGE).printInfo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}