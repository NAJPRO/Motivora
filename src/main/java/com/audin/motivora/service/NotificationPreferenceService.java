package com.audin.motivora.service;

import com.audin.motivora.dto.request.NotificationPreferenceRequest;
import com.audin.motivora.dto.response.NotificationPreferenceResponse;
import com.audin.motivora.entity.NotificationPreference;
import com.audin.motivora.entity.User;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse getMyPreferences();

    NotificationPreferenceResponse updateMyPreferences(NotificationPreferenceRequest request);

    /** Returns the user's preferences, creating the default row on first access. */
    NotificationPreference getOrCreate(User user);
}
