package com.audin.motivora.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageProperties {

    /** Directory images are written to. */
    private String location = "uploads";

    /**
     * Absolute base the stored URLs are prefixed with. Leave empty to return
     * context-relative URLs (e.g. {@code /api/uploads/avatars/x.webp}).
     */
    private String publicBaseUrl = "";

    /** Maximum accepted image size, in bytes. */
    private long maxImageSize = 5L * 1024 * 1024;
}
