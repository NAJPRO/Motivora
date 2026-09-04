package com.audin.motivora.config;

import java.nio.file.Paths;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.audin.motivora.storage.LocalFileStorageService;
import com.audin.motivora.storage.StorageProperties;

import lombok.RequiredArgsConstructor;

/**
 * Serves the images written by {@link LocalFileStorageService}.
 *
 * They are cached hard: stored filenames are UUIDs, so a given URL always maps to the same
 * bytes and a mobile client should never re-download one.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private static final int CACHE_SECONDS = 60 * 60 * 24 * 365;

    private final StorageProperties storageProperties;

    /**
     * Adds ETags to the public catalogue reads, so a client that already has the current
     * page gets a 304 instead of the payload — the single biggest saving on a slow mobile
     * connection. Scoped to those paths because the filter buffers each response.
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> catalogueEtagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registration =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        String version = "/" + ApiVersionConfig.CURRENT_VERSION;
        registration.addUrlPatterns(
                version + "/quotes/*", version + "/themes/*", version + "/authors/*");
        registration.setName("catalogueEtagFilter");
        return registration;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(this.storageProperties.getLocation())
                .toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler(LocalFileStorageService.URL_PREFIX + "**")
                .addResourceLocations(location)
                .setCachePeriod(CACHE_SECONDS);
    }
}
