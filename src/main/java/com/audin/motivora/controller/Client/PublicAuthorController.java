package com.audin.motivora.controller.Client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.service.AuthorService;

import lombok.RequiredArgsConstructor;

/**
 * Public author catalogue: needed to render an author's name and avatar next to a quote,
 * and to open an author profile from the mobile app.
 */
@RestController
@RequestMapping("authors")
@RequiredArgsConstructor
public class PublicAuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<PageResponse<AuthorResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(this.authorService.getAllActive(page, size)));
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<AuthorResponse> show(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(this.authorService.getActive(idOrSlug));
    }
}
