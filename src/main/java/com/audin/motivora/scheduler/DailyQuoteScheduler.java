package com.audin.motivora.scheduler;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.entity.DeviceToken;
import com.audin.motivora.entity.NotificationPreference;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.NotificationType;
import com.audin.motivora.notification.push.PushMessage;
import com.audin.motivora.notification.push.PushSender;
import com.audin.motivora.repository.DeviceTokenRepository;
import com.audin.motivora.repository.NotificationPreferenceRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.service.DeviceTokenService;
import com.audin.motivora.service.UserNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends the daily quote.
 *
 * Runs every hour rather than once a day, because "08:00" means 08:00 where the user is:
 * each run picks the users whose local hour has just come round. {@code lastSentOn} is
 * recorded per user, so a restart or an overlapping run cannot notify anyone twice.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.push.daily-quote.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DailyQuoteScheduler {

    private static final int PREVIEW_LENGTH = 120;

    private final NotificationPreferenceRepository preferenceRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final QuoteRepository quoteRepository;
    private final UserNotificationService userNotificationService;
    private final DeviceTokenService deviceTokenService;
    private final PushSender pushSender;
    private final DailyQuotePicker quotePicker;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendDailyQuotes() {
        List<NotificationPreference> candidates = this.preferenceRepository.findDailyCandidates();
        int sent = 0;

        for (NotificationPreference preference : candidates) {
            if (this.shouldSendNow(preference)) {
                sent += this.send(preference) ? 1 : 0;
            }
        }

        if (sent > 0) {
            log.info("Daily quote sent to {} user(s)", sent);
        }
    }

    private boolean shouldSendNow(NotificationPreference preference) {
        ZonedDateTime localNow = ZonedDateTime.now(this.zoneOf(preference));
        if (localNow.getHour() != preference.getDailyQuoteHour()) {
            return false;
        }
        LocalDate today = localNow.toLocalDate();
        return !today.equals(preference.getLastSentOn());
    }

    private boolean send(NotificationPreference preference) {
        User user = preference.getUser();

        Optional<Quote> quote = this.quotePicker.pickFor(user);
        if (quote.isEmpty()) {
            return false;
        }

        List<String> tokens = this.deviceTokenRepository.findAllByUserIdAndEnabledTrue(user.getId())
                .stream()
                .map(DeviceToken::getToken)
                .toList();
        if (tokens.isEmpty()) {
            return false;
        }

        Quote selected = quote.get();
        String title = "Votre citation du jour";
        String body = this.preview(selected.getContent());

        List<String> invalidTokens = this.pushSender.send(tokens, new PushMessage(title, body, Map.of(
                "type", NotificationType.DAILY_QUOTE.name(),
                "quoteId", String.valueOf(selected.getId()),
                "quoteSlug", selected.getSlug())));
        this.deviceTokenService.disableTokens(invalidTokens);

        this.userNotificationService.record(user, NotificationType.DAILY_QUOTE, title, body, selected);

        preference.setLastSentOn(ZonedDateTime.now(this.zoneOf(preference)).toLocalDate());
        this.preferenceRepository.save(preference);
        return true;
    }

    /** A stored zone can become invalid if the tz database drops it; fall back rather than skip the user. */
    private ZoneId zoneOf(NotificationPreference preference) {
        try {
            return ZoneId.of(preference.getTimezone());
        } catch (DateTimeException ex) {
            log.warn("Unknown timezone {} for user {}, falling back to UTC",
                    preference.getTimezone(), preference.getUser().getId());
            return ZoneId.of(NotificationPreference.DEFAULT_TIMEZONE);
        }
    }

    private String preview(String content) {
        if (content.length() <= PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_LENGTH - 1).trim() + "…";
    }
}
