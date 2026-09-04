package com.audin.motivora.notification.push;

import java.util.List;

/**
 * Delivers push messages to device tokens.
 *
 * Implementations are swapped by configuration ({@code app.push.provider}), so the daily
 * scheduler never knows whether it is talking to Expo, FCM, or nothing at all in dev.
 */
public interface PushSender {

    /**
     * Sends one message to a batch of tokens.
     *
     * @return the tokens the provider rejected as permanently invalid; the caller disables them
     */
    List<String> send(List<String> tokens, PushMessage message);
}
