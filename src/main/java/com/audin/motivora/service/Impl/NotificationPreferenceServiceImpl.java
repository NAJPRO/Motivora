package com.audin.motivora.service.Impl;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.request.NotificationPreferenceRequest;
import com.audin.motivora.dto.response.NotificationPreferenceResponse;
import com.audin.motivora.entity.NotificationPreference;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.mapper.ThemeMapper;
import com.audin.motivora.repository.NotificationPreferenceRepository;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.service.NotificationPreferenceService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;
    private final ThemeMapper themeMapper;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public NotificationPreferenceResponse getMyPreferences() {
        User user = this.reload();
        return this.toResponse(this.getOrCreate(user), user);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updateMyPreferences(NotificationPreferenceRequest request) {
        User user = this.reload();
        NotificationPreference preference = this.getOrCreate(user);

        if (request.getDailyQuoteEnabled() != null) {
            preference.setDailyQuoteEnabled(request.getDailyQuoteEnabled());
        }
        if (request.getDailyQuoteHour() != null) {
            preference.setDailyQuoteHour(request.getDailyQuoteHour());
        }
        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            preference.setTimezone(this.validZone(request.getTimezone()));
        }
        if (request.getFollowedThemeIds() != null) {
            user.setFollowedThemes(new HashSet<>(this.resolveThemes(request.getFollowedThemeIds())));
            this.userRepository.save(user);
        }

        return this.toResponse(this.preferenceRepository.save(preference), user);
    }

    @Override
    @Transactional
    public NotificationPreference getOrCreate(User user) {
        return this.preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> this.preferenceRepository.save(
                        NotificationPreference.builder()
                                .user(user)
                                .dailyQuoteEnabled(true)
                                .dailyQuoteHour(NotificationPreference.DEFAULT_HOUR)
                                .timezone(NotificationPreference.DEFAULT_TIMEZONE)
                                .build()));
    }

    private List<Theme> resolveThemes(List<Integer> themeIds) {
        if (themeIds.isEmpty()) {
            return List.of();
        }
        List<Theme> themes = this.themeRepository.findAllById(themeIds);
        if (themes.size() != themeIds.size()) {
            throw new EntityNotFoundException("One or more themes do not exist");
        }
        return themes;
    }

    private String validZone(String timezone) {
        try {
            return ZoneId.of(timezone).getId();
        } catch (DateTimeException ex) {
            throw new BusinessException("INVALID_TIMEZONE", "Unknown timezone: " + timezone);
        }
    }

    private NotificationPreferenceResponse toResponse(NotificationPreference preference, User user) {
        return new NotificationPreferenceResponse(
                preference.isDailyQuoteEnabled(),
                preference.getDailyQuoteHour(),
                preference.getTimezone(),
                user.getFollowedThemes().stream().map(this.themeMapper::toResponse).toList());
    }

    private User reload() {
        Integer id = this.authUtil.getCurrentUser().getId();
        return this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
