package com.audin.motivora.enums;

/**
 * Client platform a session was opened from. Used to label the sessions listed
 * in "my connected devices" and to route push notifications later on.
 */
public enum DevicePlatform {
    IOS,
    ANDROID,
    WEB,
    UNKNOWN;

    public static DevicePlatform from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        try {
            return DevicePlatform.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}
