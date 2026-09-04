package com.audin.motivora.controller.Client;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.FavoriteRequest;
import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.service.FavoriteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<PageResponse<FavoriteResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(this.favoriteService.getAllFavorites(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FavoriteResponse> show(@PathVariable Integer id) {
        return ResponseEntity.ok(this.favoriteService.getFavorite(id));
    }

    /**
     * Idempotent add. Preferred over the legacy toggle: on a flaky mobile connection a
     * retried toggle silently un-favourites the quote.
     */
    @PutMapping("/{quoteId}")
    public ResponseEntity<MessageResponse> add(@PathVariable Integer quoteId) {
        boolean created = this.favoriteService.addFavorite(quoteId);
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK)
                .body(MessageResponse.of("Quote added to favorites"));
    }

    @DeleteMapping("/{quoteId}")
    public ResponseEntity<MessageResponse> remove(@PathVariable Integer quoteId) {
        this.favoriteService.removeFavorite(quoteId);
        return ResponseEntity.ok(MessageResponse.of("Quote removed from favorites"));
    }

    /** Legacy toggle, kept for the clients already calling it. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> toggle(@RequestBody @Valid FavoriteRequest entity) {
        boolean added = this.favoriteService.addFavorite(entity.getQuoteId());
        if (!added) {
            this.favoriteService.removeFavorite(entity.getQuoteId());
        }
        return ResponseEntity.ok(Map.of(
                "message", added ? "Quote added to favorites" : "Quote removed from favorites",
                "isFavorite", added));
    }
}
