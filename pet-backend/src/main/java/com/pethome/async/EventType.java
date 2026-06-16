package com.pethome.async;

public enum EventType {
    LIKE("like"),
    FOLLOW("follow"),
    COMMENT("comment"),
    PUBLISH("publish"),
    REGISTER("register"),
    DELETE("delete"),
    SYSTEM("system");

    private final String value;
    EventType(String value) { this.value = value; }
    public String getValue(){ return this.value; }
}
