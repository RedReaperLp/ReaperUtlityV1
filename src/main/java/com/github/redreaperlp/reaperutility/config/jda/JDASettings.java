package com.github.redreaperlp.reaperutility.config.jda;

import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class JDASettings {
    @Nullable
    private String token;
    @Nullable
    private Activity.ActivityType activity;
    @Nullable
    private String activityText;
    @Nullable
    private final OnlineStatus status;

    public JDASettings(String token, String activity, String activityText, OnlineStatus status) {
        for (Activity.ActivityType activityType : Activity.ActivityType.values()) {
            if (activityType.name().equals(activity)) {
                this.activity = activityType;
                break;
            }
        }
        this.token = token;
        this.activityText = activityText;
        this.status = status;
    }

    public JDASettings() {
        this.token = "YourToken";
        this.activity = Activity.ActivityType.PLAYING;
        this.activityText = "with yout feelings";
        this.status = OnlineStatus.ONLINE;
    }

    public String token() {
        if (token == null) {
            try {
                new Color.Print("Token is null!", Color.RED);
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }
        return token;
    }

    public Activity.ActivityType activity() {
        if (activity == null) {
            new Color.Print("Activity is null!", Color.RED).printSuccess();
            new Color.Print("Defaulting activity to \"PLAYING\"", Color.YELLOW).printSuccess();
            this.activity = Activity.ActivityType.PLAYING;
            return Activity.ActivityType.PLAYING;
        }
        return activity;
    }

    public String activityText() {
        if (activityText == null || activityText.isEmpty()) {
            new Color.Print("ActivityText is null!", Color.RED).printSuccess();
            new Color.Print("Defaulting activityText to \"with yout feelings\"", Color.YELLOW).printSuccess();
            this.activityText = "with yout feelings";
            return "with yout feelings";
        }
        return activityText;
    }

    public OnlineStatus status() {
        return status;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
