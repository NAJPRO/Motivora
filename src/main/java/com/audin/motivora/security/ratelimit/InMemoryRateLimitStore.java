package com.audin.motivora.security.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Per-instance fixed-window counter. Adequate for a single node; see {@link RateLimitStore}
 * for what changes when the API is replicated.
 */
@Component
@ConditionalOnProperty(name = "app.rate-limit.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryRateLimitStore implements RateLimitStore {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean isExceeded(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMs = window.toMillis();

        Window current = this.windows.compute(key, (ignored, existing) ->
                (existing == null || now - existing.startMs >= windowMs) ? new Window(now) : existing);

        return current.count.incrementAndGet() > maxRequests;
    }

    /** Without this, an attacker rotating IPs would grow the map without bound. */
    @Scheduled(fixedDelay = 300_000)
    public void evictStaleWindows() {
        long cutoff = System.currentTimeMillis() - Duration.ofHours(1).toMillis();
        this.windows.entrySet().removeIf(entry -> entry.getValue().startMs < cutoff);
    }

    private static final class Window {

        private final long startMs;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startMs) {
            this.startMs = startMs;
        }
    }
}
