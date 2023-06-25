package com.github.redreaperlp.reaperutility.api;

public enum ERequest {
    GET_GUILD("get.guild"),
    MODIFY_EVENT("modify.event"),
    CHECK_USER_PERMISSION("check.user.permission"),
    GET_EVENTS("get.events"),
    GET_GUILDS("get.guilds"),
    INVALID("invalid");

    private final String request;

    ERequest(String request) {
        this.request = request;
    }

    public String request() {
        return request;
    }

    public static ERequest get(String request) {
        for (ERequest value : values()) {
            if (value.request.equals(request)) {
                return value;
            }
        }
        return INVALID;
    }

    public static class Event_Modify {
        String timestamp;
        long event;
        long guild;
        long channel;

        public Event_Modify(String timestamp, long event, long guild, long channel) {
            this.timestamp = timestamp;
            this.event = event;
            this.guild = guild;
            this.channel = channel;
        }

        public String timestamp() {
            return timestamp;
        }

        public long event() {
            return event;
        }

        public long server() {
            return guild;
        }

        public long channel() {
            return channel;
        }
    }
}
