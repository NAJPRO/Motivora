package com.audin.motivora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Sent by the app once the OS grants notification permission, and again whenever the
 * provider rotates the token.
 */
@Getter
@Setter
public class RegisterDeviceRequest {

    @NotBlank(message = "Push token is required")
    @Size(max = 255, message = "Push token is too long")
    private String token;

    /** ios | android | web. Falls back to UNKNOWN. */
    private String platform;

    @Size(max = 100)
    private String deviceId;

    @Size(max = 120)
    private String deviceName;
}
