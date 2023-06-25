package com.github.redreaperlp.reaperutility.enums;

public enum EPermission {
    EVENT_CREATE("event.create"),
    EVENT_DELETE("event.delete"),
    EVENT_EDIT("event.edit"),

    CLEAR("clear"),
    SET_PERMISSION("set.permission"),
    WEB_PANEL("web.panel"),
    NONE("none")
    ;

    private final String permission;

    EPermission(String permission) {
        this.permission = permission;
    }

    public String get() {
        return permission;
    }

    public static EPermission get(String permission) {
        for (EPermission value : values()) {
            if (value.permission.equals(permission)) {
                return value;
            }
        }
        return NONE;
    }
}
