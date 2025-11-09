package com.audin.motivora.controller.Auth;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.security.JwtService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@AllArgsConstructor
@Validated
@Slf4j
public class LogoutController {
    private final JwtService jwtService;

    @PostMapping(path = "logout")
    public ResponseEntity<Map<String, String>>  logout() {
        log.info("Tentative de déconnexion");
        this.jwtService.logout();
        return ResponseEntity.ok(Collections.singletonMap("message", "Logout successfully"));
    }

    @GetMapping("hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hey i'm logged");
    }
    
}
