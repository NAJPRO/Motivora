package com.audin.motivora.controller.Client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.service.QuoteService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class PublicQuoteController {

    private final QuoteService quoteService;

    /**
     * Liste paginée des quotes publiées
     */
    @GetMapping
    public ResponseEntity<Page<QuoteResponse>> getAllQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(quoteService.getAllQuotes(page, size));
    }

    /**
     * Détail d'une quote par ID ou slug
     */
    @GetMapping("/{idOrSlug}")
    public ResponseEntity<QuoteResponse> getQuote(
            @PathVariable String idOrSlug
    ) {
        return ResponseEntity.ok(quoteService.getQuote(idOrSlug));
    }

    /**
     * Quotes publiées par auteur
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<QuoteResponse>> getQuotesByAuthor(
            @PathVariable Integer authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                quoteService.getQuotesByAuthor(authorId, page, size)
        );
    }

    /**
     * Recherche par mot-clé
     */
    @GetMapping("/search")
    public ResponseEntity<Page<QuoteResponse>> searchQuotes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                quoteService.searchByKeyword(keyword, page, size)
        );
    }

    /**
     * Quote aléatoire
     */
    @GetMapping("/random")
    public ResponseEntity<QuoteResponse> getRandomQuote() {
        return ResponseEntity.ok(quoteService.getRandomQuote());
    }
}

