package com.audin.motivora.controller.Auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.LoginDTORequest;
import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.AuthDTOResponse;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.security.JwtService;
import com.audin.motivora.service.AuthService;
import com.audin.motivora.service.mails.WelcomeMail;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@AllArgsConstructor
@Validated
@Slf4j
@RequestMapping(path = "auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final WelcomeMail mailer;
    public static final long MAX_AGE = 60 * 60 * 1000;

    @PostMapping(path = "register")
    public ResponseEntity<AuthDTOResponse>  register(@RequestBody @Valid RegisterDTORequest dto, HttpServletResponse response) {
        Role role = new Role();
        role.setName(UserRole.USER);


        User user = this.authService.register(dto, role);
        //Envoyer le mail de bienvenu
        this.mailer.welcome(user);

        // Générer le token et le refreshToken
        Map<String, String> tokens = this.jwtService.generateToken(user.getEmail());
        String accessToken = tokens.get("Bearer");
        String refreshToken = tokens.get("refresh");

        ResponseCookie cookie = this.createHttpOnlyCookie(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        AuthDTOResponse dtoResponse = this.authMapper.authResponse(accessToken, this.authMapper.toDto(user));
        return new ResponseEntity<>(dtoResponse, HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<AuthDTOResponse> login(@RequestBody @Valid LoginDTORequest dto, HttpServletResponse response) {
        final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        if (authentication.isAuthenticated() == false) {
            throw new IllegalArgumentException("Invalid credential");
        }

        Map<String, String> tokens = this.jwtService.generateToken(dto.getEmail());
        String accessToken = tokens.get("Bearer");
        String refreshToken = tokens.get("refresh");

        ResponseCookie cookie = this.createHttpOnlyCookie( refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        User user = this.authService.findByEmail(dto.getEmail());
        AuthDTOResponse dtoResponse = this.authMapper.authResponse(accessToken, this.authMapper.toDto(user));
        return ResponseEntity.ok(dtoResponse);
    }

    @PostMapping(path = "refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken, 
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token is missing"));
        }

        Map<String, String> tokens = jwtService.refrechToken(refreshToken);
        String newRefreshToken = tokens.get("refresh");

        ResponseCookie cookie = this.createHttpOnlyCookie(newRefreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        tokens.remove(newRefreshToken);
        return ResponseEntity.ok(tokens);
    }
    private ResponseCookie createHttpOnlyCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("None") // Adjust based on your requirements : Strict Lax None
                .build();
    }

}
