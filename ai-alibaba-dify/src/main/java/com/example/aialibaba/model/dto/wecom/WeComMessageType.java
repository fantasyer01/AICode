package com.example.aialibaba.model.dto.wecom;

/**
 * WeChat Work message types for intelligent robot
 */
public enum WeComMessageType {
    TEXT("text"),
    IMAGE("image"),
    MIXED("mixed"),
    VOICE("voice"),
    FILE("file"),
    QUOTE("quote"),
    EVENT("event");

    private final String value;

    WeComMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WeComMessageType fromValue(String value) {
        for (WeComMessageType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return TEXT;
    }
}
