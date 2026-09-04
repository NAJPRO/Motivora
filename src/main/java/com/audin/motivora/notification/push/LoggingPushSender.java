package com.audin.motivora.notification.push;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Default sender: logs instead of delivering.
 *
 * Keeps the whole pipeline exercisable in development without Expo or Firebase
 * credentials — the scheduler, preferences and history all behave normally.
 */
@Service
@ConditionalOnProperty(name = "app.push.provider", havingValue = "none", matchIfMissing = true)
@Slf4j
public class LoggingPushSender implements PushSender {

    @Override
    public List<String> send(List<String> tokens, PushMessage message) {
        log.info("[push:noop] would deliver \"{}\" to {} device(s)", message.title(), tokens.size());
        return List.of();
    }
}
