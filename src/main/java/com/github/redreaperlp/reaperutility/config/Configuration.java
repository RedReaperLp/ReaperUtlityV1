package com.github.redreaperlp.reaperutility.config;

import com.github.redreaperlp.reaperutility.config.console.JConsoleSettings;
import com.github.redreaperlp.reaperutility.config.files.JFiles;
import com.github.redreaperlp.reaperutility.config.jda.JDASettings;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Configuration {

    private JDASettings jdaSettings;
    private JFiles files;
    private JConsoleSettings consoleSettings;

    public Configuration(JDASettings jdaSettings) {
        this.jdaSettings = jdaSettings;
    }

    public Configuration() {
        this.jdaSettings = new JDASettings();
        this.files = new JFiles();
    }



    public JDASettings jdaSettings() {
        return jdaSettings;
    }

    public JFiles files() {
        return files;
    }

    public void save() {
        File file = new File("config.json");
        try {
            file.delete();
            file.createNewFile();
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(this);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JConsoleSettings consoleSettings() {
        return consoleSettings;
    }

    public Configuration consoleSettings(JConsoleSettings consoleSettings) {
        this.consoleSettings = consoleSettings;
        save();
        return this;
    }

    public Configuration jdaSettings(JDASettings jdaSettings) {
        this.jdaSettings = jdaSettings;
        save();
        return this;
    }

    public Configuration files(JFiles files) {
        this.files = files;
        save();
        return this;
    }
}
