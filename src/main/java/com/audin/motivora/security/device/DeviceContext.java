package com.audin.motivora.security.device;

import com.audin.motivora.enums.DevicePlatform;

/**
 * The client installation a session belongs to, read from request headers.
 * A mobile app sends a stable per-install identifier so its session survives
 * a login made from another device.
 */
public record DeviceContext(String deviceId, String deviceName, DevicePlatform platform) {

    public boolean isIdentified() {
        return deviceId != null && !deviceId.isBlank();
    }
}
