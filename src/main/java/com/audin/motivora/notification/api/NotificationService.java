package com.audin.motivora.notification.api;

import java.util.Map;

import com.audin.motivora.notification.model.NotificationTemplateType;

public interface NotificationService {
    /**
     * Envoie une notification basée sur un modèle et des données.
     * @param target La destination (e-mail, numéro de téléphone, ID utilisateur, etc.).
     * @param templateType Le type de modèle de notification (ex: WELCOME, PASSWORD_RESET).
     * @param data Les données nécessaires pour remplir le modèle.
     */
    void sendNotification(String target, NotificationTemplateType templateType, Map<String, Object> data);
}
