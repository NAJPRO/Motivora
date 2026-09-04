package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.service.AuthorService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
@Validated
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<PageResponse<AuthorResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.from(authorService.getAll(page, size)));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> save(@RequestBody @Valid AuthorRequest entity) {

        return ResponseEntity.ok(authorService.save(entity));
    }

    @PutMapping("/{idOrSlug}")
    public ResponseEntity<AuthorResponse> update(@PathVariable String idOrSlug, @RequestBody @Valid AuthorRequest entity) {
        return ResponseEntity.ok(authorService.update(idOrSlug, entity));
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<AuthorResponse> show(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(authorService.create(idOrSlug));
    }

    @PutMapping("/{idOrSlug}/disable")
    public ResponseEntity<MessageResponse> disable(@PathVariable String idOrSlug) {
        authorService.disable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Auteur désactivé avec succès"));
    }

    @PutMapping("/{idOrSlug}/enable")
    public ResponseEntity<MessageResponse> enable(@PathVariable String idOrSlug) {
        authorService.enable(idOrSlug);
        return ResponseEntity.ok(MessageResponse.of("Auteur activé avec succès"));
    }
    
}
