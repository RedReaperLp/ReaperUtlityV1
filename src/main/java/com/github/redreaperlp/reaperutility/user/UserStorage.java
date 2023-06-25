package com.github.redreaperlp.reaperutility.user;

import com.github.redreaperlp.reaperutility.events.Configurator;
import com.github.redreaperlp.reaperutility.util.Color;

public class UserStorage {
    private final String userID;
    private final String name;

    private int timeUntilForget = 180;
    private int rateLimit;
    private boolean isRateLimited = false;
    private Configurator currentConfigurator;
    private boolean isNotified;
    private boolean clearActive = false;

    public UserStorage(String userID, String name) {
        this.userID = userID;
        this.name = name;
        this.rateLimit = 0;
    }

    public String userID() {
        return userID;
    }

    public String userName() {
        return name;
    }

    public int rateLimit() {
        return rateLimit;
    }

    public UserStorage increaseRateLimit() {
        rateLimit++;
        if (rateLimit >= 5) {
            isRateLimited = true;
        }
        return this;
    }

    public UserStorage decreaseRateLimit() {
        if (rateLimit > 0)
            rateLimit--;
        if (rateLimit <= 0) {
            if (isNotified)
                new Color.Print("User is no longer rate limited: " + name + " (" + userID + ")", Color.GREEN).printWarning();
            isRateLimited = false;
            isNotified = false;
        }
        return this;
    }

    public UserStorage currentConfigurator(Configurator configurator) {
        this.currentConfigurator = configurator;
        return this;
    }

    public Configurator currentConfigurator() {
        return currentConfigurator;
    }

    public boolean canBeForget() {
        timeUntilForget--;
        return timeUntilForget <= 0 && rateLimit <= 0;
    }

    public int timeUntilForget() {
        return timeUntilForget;
    }

    public UserStorage resetTimeUntilForget() {
        timeUntilForget = 180;
        return this;
    }

    public boolean isRateLimited() {
        return isRateLimited;
    }

    public boolean isNotified() {
        boolean isNotified = this.isNotified;
        if (!isNotified)
            new Color.Print("User reached rate limit: " + name + " (" + userID + ")", Color.RED).printWarning();
        this.isNotified = true;
        return isNotified;
    }

    public boolean clearIsActive() {
        return clearActive;
    }

    public void clearInactive() {
        clearActive = false;
    }

    public void clearActive() {
        clearActive = true;
    }
}
