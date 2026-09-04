package com.audin.motivora.service.Impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.response.NotificationResponse;
import com.audin.motivora.entity.Notification;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.NotificationType;
import com.audin.motivora.repository.NotificationRepository;
import com.audin.motivora.service.UserNotificationService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMine(int page, int size) {
        Integer userId = this.authUtil.getCurrentUser().getId();
        return this.notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return this.notificationRepository.countByUserIdAndIsReadFalse(
                this.authUtil.getCurrentUser().getId());
    }

    @Override
    @Transactional
    public void markAsRead(Integer id) {
        Integer userId = this.authUtil.getCurrentUser().getId();
        Notification notification = this.notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        this.notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        this.notificationRepository.markAllAsRead(
                this.authUtil.getCurrentUser().getId(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public Notification record(User user, NotificationType type, String title, String message, Quote quote) {
        return this.notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .quote(quote)
                .build());
    }

    private NotificationResponse toResponse(Notification notification) {
        Quote quote = notification.getQuote();
        return new NotificationResponse(
                notification.getId(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                quote != null ? quote.getId() : null,
                quote != null ? quote.getSlug() : null,
                Boolean.TRUE.equals(notification.getIsRead()),
                notification.getReadAt(),
                notification.getCreatedAt());
    }
}
