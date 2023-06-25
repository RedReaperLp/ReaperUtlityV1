package com.github.redreaperlp.reaperutility.user;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserManager implements Runnable {
    private static final List<UserStorage> users = new ArrayList<>();
    private static Thread thread = new Thread(new UserManager());

    public static UserStorage getUser(User user) {
        for (UserStorage userStorage : users) {
            if (userStorage.userID().equals(user.getId())) {
                return userStorage;
            }
        }
        UserStorage userStorage = new UserStorage(user.getId(), user.getName());
        users.add(userStorage);
        if (!thread.isAlive()) {
            thread = new Thread(new UserManager());
            thread.start();
        }
        return userStorage;
    }

    public static void removeUser(User user) {
        UserStorage toRemove = null;
        for (UserStorage userStorage : users) {
            if (userStorage.userID().equals(user.getId())) {
                toRemove = userStorage;
            }
        }
        users.remove(toRemove);
    }

    @Override
    public void run() {
        while (!users.isEmpty()) {
            List<UserStorage> toRemove = new ArrayList<>();
            for (UserStorage userStorage : users) {
                userStorage.decreaseRateLimit();
                if (userStorage.canBeForget()) {
                    toRemove.add(userStorage);
                }
            }
            users.removeAll(toRemove);
            try {
                TimeUnit.SECONDS.sleep(6);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isUserRateLimited(UserStorage storage) {
        boolean isRateLimited = storage.isRateLimited();
        storage.increaseRateLimit();
        return isRateLimited;
    }
}
