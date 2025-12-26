package com.audin.motivora.service.mails;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.audin.motivora.entity.User;
import com.audin.motivora.notification.api.NotificationService;
import com.audin.motivora.notification.model.NotificationTemplateType;
import com.audin.motivora.utils.OtpGenerator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WelcomeMail {
    private final NotificationService notificationService;

    public void welcome(User user) {

        // Préparer les données pour l'e-mail
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("userName", user.getPseudo());

        notificationService.sendNotification(
                user.getEmail(),
                NotificationTemplateType.WELCOME,
                emailData);
    }
}
