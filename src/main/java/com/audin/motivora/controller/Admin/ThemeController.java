package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.service.ThemeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "admin/themes")
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> index() {
        return ResponseEntity.ok(this.themeService.getAll());
    }
    
    @GetMapping(path = "/{idOrSlug}")
    public ResponseEntity<ThemeResponse> create(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(this.themeService.create(idOrSlug));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> save(@RequestBody @Valid ThemeRequest entity) {
        return ResponseEntity.ok(this.themeService.save(entity));
    }

    @PutMapping("/{idOrSlug}")
    public ResponseEntity<ThemeResponse> update(@PathVariable String idOrSlug, @RequestBody @Valid ThemeRequest entity) {
        return ResponseEntity.ok(themeService.update(idOrSlug, entity));
    }
    
    @PutMapping("/{idOrSlug}/disable")
    public ResponseEntity<MessageResponse> disable(@PathVariable String idOrSlug) {
        themeService.disable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Thème désactivé avec succès"));
    }

    @PutMapping("/{idOrSlug}/enable")
    public ResponseEntity<MessageResponse> enable(@PathVariable String idOrSlug) {
        themeService.enable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Thème activé avec succès"));
    }
    
}
