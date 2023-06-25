package com.github.redreaperlp.reaperutility.storage.server;

import com.github.redreaperlp.reaperutility.enums.EPermission;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.event.JEvent;
import com.github.redreaperlp.reaperutility.storage.server.permission.JPermission;
import com.github.redreaperlp.reaperutility.util.Color;

import java.util.ArrayList;
import java.util.List;

public class JGuild {
    long id;


    List<JPermission> permissions;
    List<JEvent> events;

    public JGuild(long id, List<JPermission> permissions, List<JEvent> events) {
        this.id = id;
        this.permissions = permissions;
        this.events = events;
    }

    public JGuild(long id, List<JPermission> permissions) {
        this(id, permissions, null);
    }

    public JGuild(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public JPermission getPermission(EPermission ePermission) {
        JPermission permission = null;
        if (permissions == null) permissions = new ArrayList<>();
        for (JPermission jPermission : permissions) {
            if (jPermission.permission().equals(ePermission.get())) {
                permission = jPermission;
                break;
            }
        }
        if (permission == null) {
            permission = new JPermission(false, ePermission.get());
        }
        return permission;
    }

    public JPermission getPermission(String permission) {
        EPermission ePermission = EPermission.get(permission);
        return getPermission(ePermission);
    }

    public void removePermission(EPermission permission) {
        if (permissions != null)
            permissions.remove(getPermission(permission));
        JStorage.instance().changes();

    }

    public void removePermission(String permission) {
        Color.printTest(permission);
        if (permissions != null)
            permissions.remove(getPermission(permission));
        JStorage.instance().changes();

    }

    public List<JEvent> getEvents() {
        if (events == null) events = new ArrayList<>();
        return events;
    }

    public List<JPermission> getPermissions() {
        if (permissions == null) permissions = new ArrayList<>();
        return permissions;
    }

    public JEvent getEvent(long messageID) {
        return events.stream().filter(jEvent -> jEvent.eventMessageID() == messageID).findFirst().orElse(null);
    }

    public void addPermission(JPermission jPermission) {
        if (permissions == null) permissions = new ArrayList<>();
        boolean found = false;
        for (JPermission permission : permissions) {
            if (permission.permission().equals(jPermission.permission())) {
                found = true;
                break;
            }
        }
        if (!found) {
            permissions.add(jPermission);
            JStorage.instance().changes();
        }
    }

    public void addEvent(JEvent jEvent) {
        if (events == null) events = new ArrayList<>();
        events.add(jEvent);
        JStorage.instance().changes();
    }


    public void removeEvent(long messageID) {
        if (events != null)
            events.remove(getEvent(messageID));
        JStorage.instance().changes();
    }

    public void setEvents(List<JEvent> events) {
        this.events = events;
    }

    public void setPermissions(List<JPermission> permissions) {
        this.permissions = permissions;
    }
}
