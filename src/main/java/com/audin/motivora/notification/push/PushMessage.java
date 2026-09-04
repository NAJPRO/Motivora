package com.audin.motivora.notification.push;

import java.util.Map;

/**
 * A push payload, independent of the provider that delivers it.
 *
 * {@code data} is the silent payload the app reads on tap (e.g. {@code quoteSlug}) to open
 * the right screen instead of just the home screen.
 */
public record PushMessage(String title, String body, Map<String, String> data) {
}
