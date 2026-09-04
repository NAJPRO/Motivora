package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.dto.request.QuoteRequest;
import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.service.QuoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("admin/quotes")
@RequiredArgsConstructor
@Validated
public class AdminQuoteController {
    private final QuoteService quoteService;

    @GetMapping
    public ResponseEntity<PageResponse<QuoteResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(PageResponse.from(quoteService.getAllQuotesForAdmin(status, page, size)));
    }

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
    public ResponseEntity<MessageResponse> disable(@PathVariable String idOrSlug) {
        quoteService.disable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Motivation désactivée avec succès"));
    }

    @PutMapping("/{idOrSlug}/enable")
    public ResponseEntity<MessageResponse> enable(@PathVariable String idOrSlug) {
        quoteService.enable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Motivation activée avec succès"));
    }
}
