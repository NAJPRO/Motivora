package com.audin.motivora.scheduler;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.repository.QuoteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Chooses the quote a given user receives today.
 *
 * Drawn from the themes they follow when they follow any, from the whole published
 * catalogue otherwise. The pick is derived from the date and the user id, so two people
 * do not all get the same quote and one person gets a different one each day.
 */
@Component
@RequiredArgsConstructor
public class DailyQuotePicker {

    private final QuoteRepository quoteRepository;

    public Optional<Quote> pickFor(User user) {
        Set<Theme> followed = user.getFollowedThemes();
        long seed = LocalDate.now(ZoneOffset.UTC).toEpochDay() * 31 + user.getId();

        if (followed != null && !followed.isEmpty()) {
            List<Integer> themeIds = followed.stream().map(Theme::getId).toList();
            long total = this.quoteRepository.countPublishedInThemes(QuoteStatus.PUBLISHED, themeIds);
            if (total > 0) {
                return this.quoteRepository.findPublishedInThemesOrderedById(
                                QuoteStatus.PUBLISHED, themeIds,
                                PageRequest.of((int) Math.floorMod(seed, total), 1))
                        .getContent().stream().findFirst();
            }
        }

        long total = this.quoteRepository.countPublished(QuoteStatus.PUBLISHED);
        if (total == 0) {
            return Optional.empty();
        }
        return this.quoteRepository.findPublishedOrderedById(
                        QuoteStatus.PUBLISHED, PageRequest.of((int) Math.floorMod(seed, total), 1))
                .getContent().stream().findFirst();
    }
}
