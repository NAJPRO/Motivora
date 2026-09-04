package com.audin.motivora.security.ratelimit;

import java.time.Duration;

/**
 * Counts requests per key within a fixed window.
 *
 * Extracted behind an interface so a multi-instance deployment can swap in a shared
 * backend (Redis, Bucket4j) without touching the filter: the in-memory implementation
 * only limits per instance, which N replicas turn into an N-fold limit.
 */
public interface RateLimitStore {

    /**
     * Registers a hit and reports whether the key is now over its quota.
     *
     * @return true when the request should be rejected
     */
    boolean isExceeded(String key, int maxRequests, Duration window);
}
