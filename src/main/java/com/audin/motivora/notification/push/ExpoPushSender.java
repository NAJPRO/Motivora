package com.audin.motivora.notification.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Delivers through Expo's push service, which fronts both APNs and FCM — the natural fit
 * for an Expo client, and the only provider that needs no per-store credentials to start.
 *
 * Expo answers with one ticket per message, in order. A {@code DeviceNotRegistered} error
 * means the app was uninstalled: that token is returned so the caller stops using it.
 */
@Service
@ConditionalOnProperty(name = "app.push.provider", havingValue = "expo")
@Slf4j
public class ExpoPushSender implements PushSender {

    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final PushProperties properties;
    private final RestClient restClient;

    public ExpoPushSender(PushProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        RestClient.Builder builder = restClientBuilder
                .baseUrl(properties.getExpoEndpoint())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (!properties.getExpoAccessToken().isBlank()) {
            builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getExpoAccessToken());
        }
        this.restClient = builder.build();
    }

    @Override
    public List<String> send(List<String> tokens, PushMessage message) {
        List<String> invalidTokens = new ArrayList<>();

        for (int start = 0; start < tokens.size(); start += this.properties.getBatchSize()) {
            List<String> batch = tokens.subList(
                    start, Math.min(start + this.properties.getBatchSize(), tokens.size()));
            invalidTokens.addAll(this.sendBatch(batch, message));
        }
        return invalidTokens;
    }

    private List<String> sendBatch(List<String> tokens, PushMessage message) {
        List<Map<String, Object>> payload = tokens.stream().map(token -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("to", token);
            entry.put("title", message.title());
            entry.put("body", message.body());
            entry.put("sound", "default");
            if (message.data() != null && !message.data().isEmpty()) {
                entry.put("data", message.data());
            }
            return entry;
        }).toList();

        try {
            ExpoResponse response = this.restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(ExpoResponse.class);

            return this.collectInvalidTokens(tokens, response);
        } catch (Exception ex) {
            // A provider outage must not abort the whole daily run.
            log.error("Expo push delivery failed for {} token(s): {}", tokens.size(), ex.getMessage());
            return List.of();
        }
    }

    private List<String> collectInvalidTokens(List<String> tokens, ExpoResponse response) {
        if (response == null || response.data() == null) {
            return List.of();
        }

        List<String> invalid = new ArrayList<>();
        List<ExpoTicket> tickets = response.data();
        for (int index = 0; index < tickets.size() && index < tokens.size(); index++) {
            ExpoTicket ticket = tickets.get(index);
            if ("error".equals(ticket.status())
                    && ticket.details() != null
                    && DEVICE_NOT_REGISTERED.equals(ticket.details().get("error"))) {
                invalid.add(tokens.get(index));
            }
        }
        return invalid;
    }

    // Package-private so Jackson can bind them without reflective access tricks.
    record ExpoResponse(List<ExpoTicket> data) {
    }

    record ExpoTicket(String status, String message, Map<String, String> details) {
    }
}
