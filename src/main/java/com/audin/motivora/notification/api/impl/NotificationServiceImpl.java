package com.audin.motivora.notification.api.impl;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.audin.motivora.notification.api.NotificationService;
import com.audin.motivora.notification.model.NotificationTemplateType;
import com.audin.motivora.notification.service.TemplateService;
import com.audin.motivora.notification.transporteur.NotificationTransporter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    // Liste de TOUS les transporteurs injectés par Spring (Email, SMS, etc.)
    private final List<NotificationTransporter> transporters;
    private final TemplateService templateService;


    // Une méthode utilitaire pour mapper le type d'enum à un nom de fichier de
    // template
    private String resolveTemplateName(NotificationTemplateType templateType) {
        // Ex: WELCOME -> "mails/welcome-email" (si vos fichiers sont dans
        // /templates/mails/)
        return "mails/" + templateType.name().toLowerCase() + "-email";
    }

    @Override
    @Async
    public void sendNotification(String target, NotificationTemplateType templateType, Map<String, Object> data) {

        // 1. Déterminer le nom du template et du sujet
        String templateName = resolveTemplateName(templateType);

        // IMPORTANT : Pour simplifier, nous mettons le sujet en dur ici.
        // Dans une version plus robuste, le TemplateService pourrait le retourner.
        String subject = "Mon Application - " + templateType.name().replace("_", " ");

        // 2. Génération du contenu (via Thymeleaf)
        String body = templateService.buildContent(templateName, data);

        // 3. Orchestration et Envoi
        for (NotificationTransporter transporter : transporters) {

            // Vérifie si le transporteur actuel supporte ce type de notification
            if (transporter.supports(templateType)) {
                transporter.transport(target, subject, body);
            }
        }
    }

}
