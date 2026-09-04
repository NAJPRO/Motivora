package com.audin.motivora.service;

import org.springframework.data.domain.Page;

import com.audin.motivora.dto.response.NotificationResponse;
import com.audin.motivora.entity.Notification;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.NotificationType;

/**
 * The in-app notification centre: the history a user sees in the app, which survives a
 * dismissed OS banner.
 */
public interface UserNotificationService {

    Page<NotificationResponse> listMine(int page, int size);

    long countUnread();

    void markAsRead(Integer id);

    void markAllAsRead();

    Notification record(User user, NotificationType type, String title, String message, Quote quote);
}
