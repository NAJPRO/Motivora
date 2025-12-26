package com.audin.motivora.controller.Admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.service.AuthorService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class AuthorController {
    private AuthorService authorService;
    @PostMapping
    public ResponseEntity<AuthorResponse> save(@RequestBody @Valid AuthorRequest entity) {

        return ResponseEntity.ok(authorService.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> update(@PathVariable String idOrSlug, @RequestBody AuthorRequest entity) {
        return ResponseEntity.ok(authorService.update(idOrSlug, entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> show(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(authorService.create(idOrSlug));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Map<String, String>> disable(@PathVariable String idOrSlug) {
        authorService.disable(idOrSlug);        
        return new ResponseEntity<>(Collections.singletonMap("message", "Autheur désactivé avec success"), HttpStatus.ACCEPTED);
    }
    
    @PutMapping("/{id}/enable")
    public ResponseEntity<Map<String, String>> enable(@PathVariable String idOrSlug) {
        authorService.enable(idOrSlug);        
        return new ResponseEntity<>(Collections.singletonMap("message", "Autheur activée avec success"), HttpStatus.ACCEPTED);
    }
    
}
