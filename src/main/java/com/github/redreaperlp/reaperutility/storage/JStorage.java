package com.github.redreaperlp.reaperutility.storage;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JStorage {
    private static JStorage instance;
    private List<JGuild> guilds;
    transient private boolean changes = false;

    public JStorage(boolean b) {
        init();
        if (instance == null) {
            instance = new JStorage();
        }
    }

    public static JStorage instance() {
        return instance;
    }

    public JStorage() {
        this.guilds = new ArrayList<>();
    }

    public JStorage(List<JGuild> guilds) {
        this.guilds = guilds;
        instance = this;
    }

    public void init() {
        File file = new File(Main.conf.files().serverFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
                instance = new JStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                BufferedReader reader = new BufferedReader(new java.io.FileReader(file));
                String json = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    json += line;
                }
                instance = new Gson().fromJson(json, JStorage.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void changes() {
        changes = true;
    }


    public JGuild getGuild(boolean shouldCreateWhenNotFound, long id) {
        if (guilds == null) {
            guilds = new ArrayList<>();
            if (!shouldCreateWhenNotFound) {
                return null;
            }
        }
        JGuild jGuild = guilds.stream().filter(guild -> guild.id() == id).findFirst().orElse(null);
        if (jGuild == null && shouldCreateWhenNotFound) {
            jGuild = new JGuild(id);
            guilds.add(jGuild);
            changes();
        }
        return jGuild;
    }

    public void addGuild(JGuild jGuild) {
        guilds.add(jGuild);
        changes();
    }

    public List<JGuild> getGuilds() {
        return guilds;
    }

    public void setGuilds(List<JGuild> guilds) {
        this.guilds = guilds;
        changes();
    }

    public boolean getChanges() {
        return changes;
    }

    public void setChanges(boolean b) {
        changes = b;
    }

    public void removeGuild(long idLong) {
        guilds.removeIf(guild -> guild.id() == idLong);
        changes();
    }
}
