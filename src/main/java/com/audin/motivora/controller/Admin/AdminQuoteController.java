package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.service.QuoteService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("admin/quotes")
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class AdminQuoteController {
    private QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> save(@RequestBody @Valid QuoteRequest entity) {
        return ResponseEntity.ok(quoteService.save(entity));
    }

    @PutMapping("/{idOrSlug}")
    public ResponseEntity<QuoteResponse> update(@PathVariable String idOrSlug,
            @RequestBody @Valid QuoteRequest entity) {
        return ResponseEntity.ok(quoteService.update(idOrSlug, entity));
    }

    @PutMapping("/{idOrSlug}/disable")
    public ResponseEntity<String> disable(@PathVariable String idOrSlug) {
        quoteService.disable(idOrSlug);
        return ResponseEntity.ok("Thème désactivé avec succès");
    }

    @PutMapping("/{idOrSlug}/enable")
    public ResponseEntity<String> enable(@PathVariable String idOrSlug) {
        quoteService.enable(idOrSlug);
        return ResponseEntity.ok("Thème activé avec succès");
    }
}
