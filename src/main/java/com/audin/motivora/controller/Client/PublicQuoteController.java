package com.audin.motivora.controller.Client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.service.QuoteService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<PageResponse<QuoteResponse>> getAllQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PageResponse.from(quoteService.getAllQuotes(page, size)));
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
    public ResponseEntity<PageResponse<QuoteResponse>> getQuotesByAuthor(
            @PathVariable Integer authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PageResponse.from(quoteService.getQuotesByAuthor(authorId, page, size)));
    }

    /**
     * Recherche par mot-clé
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponse<QuoteResponse>> searchQuotes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PageResponse.from(quoteService.searchByKeyword(keyword, page, size)));
    }

    /**
     * Quotes publiées d'un thème (id ou slug)
     */
    @GetMapping("/theme/{themeIdOrSlug}")
    public ResponseEntity<PageResponse<QuoteResponse>> getQuotesByTheme(
            @PathVariable String themeIdOrSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PageResponse.from(quoteService.getQuotesByTheme(themeIdOrSlug, page, size)));
    }

    /**
     * Quote aléatoire
     */
    @GetMapping("/random")
    public ResponseEntity<QuoteResponse> getRandomQuote() {
        return ResponseEntity.ok(quoteService.getRandomQuote());
    }

    /**
     * Citation du jour : identique pour tous les clients pendant 24 h (UTC).
     * C'est l'écran d'accueil de l'application mobile.
     */
    @GetMapping("/daily")
    public ResponseEntity<QuoteResponse> getQuoteOfTheDay() {
        return ResponseEntity.ok(quoteService.getQuoteOfTheDay());
    }
}

