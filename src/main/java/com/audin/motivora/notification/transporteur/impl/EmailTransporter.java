package com.audin.motivora.notification.transporteur.impl;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.audin.motivora.notification.model.NotificationTemplateType;
import com.audin.motivora.notification.transporteur.NotificationTransporter;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailTransporter implements NotificationTransporter {

    private final JavaMailSender mailSender;
    private final String senderEmail = "no-reply@motivora.com";

    @Override
    public boolean supports(NotificationTemplateType templateType) {
        return true;
    }

    /**
     * Construit et envoie l'e-mail en utilisant le JavaMailSender.
     * @param target L'adresse e-mail du destinataire.
     * @param subject Le sujet de l'e-mail.
     * @param body Le corps de l'e-mail (en HTML).
     */

    @Override
    public void transport(String target, String subject, String body) {
        try {
            // Crée un nouveau MimeMessage pour gérer le contenu complexe de l'e-mail
            MimeMessage message = mailSender.createMimeMessage();

            // Le MimeMessageHelper simplifie la configuration du message
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // Supporte les ressources et les pièces jointes
                    StandardCharsets.UTF_8.name());

            helper.setFrom(senderEmail);
            helper.setTo(target);
            helper.setSubject(subject);

            // 'true' indique que le corps de l'e-mail est en HTML
            helper.setText(body, true);

            mailSender.send(message);

            System.out.println("Email envoyé avec succès à: " + target + " avec le sujet: " + subject);

        } catch (Exception e) {
            // A voir plus tard
            
            System.err.println("Erreur lors de l'envoi de l'e-mail à " + target + ": " + e.getMessage());
            // Exemple : throw new NotificationException("Échec de l'envoi de l'e-mail", e);
        }
    }
}
