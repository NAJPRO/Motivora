package com.audin.motivora.controller.Client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.service.ThemeService;

import lombok.RequiredArgsConstructor;

/**
 * Public theme catalogue. The admin endpoints live under {@code /admin/themes} and are
 * ADMIN-only, so mobile clients need this read-only view to build their browse screen.
 */
@RestController
@RequestMapping("themes")
@RequiredArgsConstructor
public class PublicThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> index() {
        return ResponseEntity.ok(this.themeService.getAllActive());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<ThemeResponse> show(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(this.themeService.getActive(idOrSlug));
    }
}
