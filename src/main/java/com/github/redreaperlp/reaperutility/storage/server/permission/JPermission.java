package com.github.redreaperlp.reaperutility.storage.server.permission;

import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JPermission {
    boolean everyone = false;
    String permission;
    List<Long> role;


    public JPermission(String permission, boolean everyone, @NotNull List<Long> role) {
        this.permission = permission;
        this.everyone = everyone;
        this.role = role;
    }

    public JPermission(String permission, boolean everyone) {
        this(permission, everyone, new ArrayList<>());
    }

    public JPermission(String permission) {
        this(permission, false);
    }

    public JPermission(boolean everyone, String permission) {
        this(permission, everyone, new ArrayList<>());
    }

    public JPermission(boolean everyone) {
        this("", everyone, new ArrayList<>());
    }

    public void everyone(boolean everyone) {
        this.everyone = everyone;
        JStorage.instance().changes();

    }

    public boolean everyone() {
        return everyone;
    }

    public void addRole(long id) {
        role.add(id);
        JStorage.instance().changes();

    }

    public void removeRole(long id) {
        role.remove(id);
    }

    public String permission() {
        return permission;
    }

    public boolean hasPermission(Member member) {
        if (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) return true;
        if (everyone) return true;
        if (member.getRoles().stream().anyMatch(role -> this.role.contains(role.getIdLong()))) return true;
        return false;
    }

    public boolean toggleRole(Role role, JGuild jGuild) {
        boolean roleExists = this.role.contains(role.getIdLong());
        if (role.getName().equals("@everyone")) {
            this.everyone = !this.everyone;
            if (!this.everyone && this.role.isEmpty()) {
                jGuild.removePermission(this.permission);
            }
            JStorage.instance().changes();
            return this.everyone;
        }
        if (roleExists) {
            this.role.remove(role.getIdLong());
            if (this.role.isEmpty() && !this.everyone)
                jGuild.removePermission(this.permission);
            JStorage.instance().changes();
            return false;
        } else {
            this.role.add(role.getIdLong());
            JStorage.instance().changes();
            return true;
        }
    }



    public boolean hasRole(long idLong) {
        if (everyone) return true;
        return role.contains(idLong);
    }
}
