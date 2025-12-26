package com.audin.motivora.controller.Client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.FavoriteRequest;
import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.service.FavoriteService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping(path = "favorites")
public class FavoriteController {
    private FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> index(@RequestParam String param) {
        return ResponseEntity.ok(this.favoriteService.getAllFavorites());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FavoriteResponse> create(@RequestParam Integer id) {
        return ResponseEntity.ok(this.favoriteService.getFavorite(id));
    }

    @PostMapping
    public ResponseEntity<String> save(@RequestBody FavoriteRequest entity) {
        this.favoriteService.toogleFavoriteQuote(entity.getQuoteId());
        return ResponseEntity.ok("Successfuly");
    }
    

}
