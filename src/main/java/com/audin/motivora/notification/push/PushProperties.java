package com.audin.motivora.notification.push;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.push")
@Getter
@Setter
public class PushProperties {

    /** {@code expo} to deliver through Expo's push service, {@code none} to only log. */
    private String provider = "none";

    /** Expo push endpoint. */
    private String expoEndpoint = "https://exp.host/--/api/v2/push/send";

    /** Optional Expo access token, required only when push security is enabled on the project. */
    private String expoAccessToken = "";

    /** Expo accepts at most 100 messages per request. */
    private int batchSize = 100;
}
