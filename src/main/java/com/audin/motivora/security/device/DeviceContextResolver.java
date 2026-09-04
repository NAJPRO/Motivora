package com.audin.motivora.security.device;

import org.springframework.stereotype.Component;

import com.audin.motivora.enums.DevicePlatform;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Builds a {@link DeviceContext} from the request headers a client is expected to send:
 *
 * <pre>
 *   X-Device-Id:       stable per-install identifier (expo-application / UUID stored in SecureStore)
 *   X-Device-Name:     human label shown in "my connected devices" (e.g. "iPhone 15 de Audin")
 *   X-Client-Platform: ios | android | web
 * </pre>
 *
 * Headers are optional: a client that sends none gets an unidentified session, which
 * behaves like the previous single-session model.
 */
@Component
public class DeviceContextResolver {

    public static final String DEVICE_ID_HEADER = "X-Device-Id";
    public static final String DEVICE_NAME_HEADER = "X-Device-Name";
    public static final String PLATFORM_HEADER = "X-Client-Platform";

    private static final int MAX_DEVICE_ID_LENGTH = 100;
    private static final int MAX_DEVICE_NAME_LENGTH = 120;

    public DeviceContext resolve(HttpServletRequest request) {
        return new DeviceContext(
                truncate(request.getHeader(DEVICE_ID_HEADER), MAX_DEVICE_ID_LENGTH),
                truncate(request.getHeader(DEVICE_NAME_HEADER), MAX_DEVICE_NAME_LENGTH),
                DevicePlatform.from(request.getHeader(PLATFORM_HEADER)));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
