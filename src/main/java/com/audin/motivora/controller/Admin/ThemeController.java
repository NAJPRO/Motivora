package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.service.ThemeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
@Validated
@RequestMapping(path = "admin/themes")
public class ThemeController {
    private ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> index(@RequestParam String param) {
        return ResponseEntity.ok(this.themeService.getAll());
    }
    
    @GetMapping(path = "/{id}")
    public ResponseEntity<ThemeResponse> create(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(this.themeService.create(idOrSlug));
    }

    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid ThemeRequest entity) {
        this.themeService.save(entity);
        return ResponseEntity.ok("Successfuly");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThemeResponse> update(@PathVariable String idOrSlug, @RequestBody @Valid ThemeRequest entity) {
        return ResponseEntity.ok(themeService.update(idOrSlug, entity));
    }
    
    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disable(@PathVariable String idOrSlug) {
       themeService.disable(idOrSlug);
        return ResponseEntity.ok("Disable successfuly");
    }
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enable(@PathVariable String idOrSlug) {
       themeService.enable(idOrSlug);
        return ResponseEntity.ok("Enable successfuly");
    }
    
}
