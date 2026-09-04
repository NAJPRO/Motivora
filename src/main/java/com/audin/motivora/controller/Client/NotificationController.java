package com.audin.motivora.controller.Client;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.NotificationPreferenceRequest;
import com.audin.motivora.dto.response.NotificationPreferenceResponse;
import com.audin.motivora.dto.response.NotificationResponse;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.service.NotificationPreferenceService;
import com.audin.motivora.service.UserNotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * In-app notification centre and delivery preferences.
 */
@RestController
@RequestMapping("me/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final UserNotificationService userNotificationService;
    private final NotificationPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(this.userNotificationService.listMine(page, size)));
    }

    /** Feeds the badge on the app icon and the bell in the UI. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("unread", this.userNotificationService.countUnread()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Integer id) {
        this.userNotificationService.markAsRead(id);
        return ResponseEntity.ok(MessageResponse.of("Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        this.userNotificationService.markAllAsRead();
        return ResponseEntity.ok(MessageResponse.of("All notifications marked as read"));
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> preferences() {
        return ResponseEntity.ok(this.preferenceService.getMyPreferences());
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @RequestBody @Valid NotificationPreferenceRequest request) {
        return ResponseEntity.ok(this.preferenceService.updateMyPreferences(request));
    }
}
