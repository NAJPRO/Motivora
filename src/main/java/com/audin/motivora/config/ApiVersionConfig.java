package com.audin.motivora.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;

import org.springframework.web.method.HandlerTypePredicate;

/**
 * Prefixes every controller with {@code /v1}, so the API is served at {@code /api/v1/**}.
 *
 * An installed mobile app cannot be forced to upgrade: without a version segment, any
 * breaking change would break the copies already on people's phones. Versioning is applied
 * here rather than in each {@code @RequestMapping} so no controller can forget it.
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    public static final String CURRENT_VERSION = "v1";

    private static final String CONTROLLER_BASE_PACKAGE = "com.audin.motivora.controller";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                "/" + CURRENT_VERSION,
                HandlerTypePredicate.forBasePackage(CONTROLLER_BASE_PACKAGE));
    }
}
