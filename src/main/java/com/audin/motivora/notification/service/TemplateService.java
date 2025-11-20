package com.audin.motivora.notification.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final SpringTemplateEngine templateEngine;

    /**
     * Génère le corps du message à partir d'un template Thymeleaf et des données.
     * @param templateName Le chemin du template (ex: "mail/welcome").
     * @param data Les variables à injecter dans le template.
     * @return Le contenu rendu (ex: une chaîne HTML).
     */
    public String buildContent(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data); 

        // Traitement du template (le chemin est relatif à src/main/resources/templates/)
        return templateEngine.process(templateName, context);
    }
}
