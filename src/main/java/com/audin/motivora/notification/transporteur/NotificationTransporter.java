package com.audin.motivora.notification.transporteur;

import com.audin.motivora.notification.model.NotificationTemplateType;

public interface NotificationTransporter {
    // Déclare si ce transporteur est capable de gérer ce type de template.
    boolean supports(NotificationTemplateType templateType);

    // Méthode pour l'envoi réel
    void transport(String target, String subject, String body);
}
