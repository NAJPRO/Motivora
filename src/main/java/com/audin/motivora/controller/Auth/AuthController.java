package com.audin.motivora.controller.Auth;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.LoginDTORequest;
import com.audin.motivora.dto.request.RefreshTokenRequest;
import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.AuthDTOResponse;
import com.audin.motivora.dto.response.TokenPairResponse;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.security.JwtService;
import com.audin.motivora.security.device.DeviceContext;
import com.audin.motivora.security.device.DeviceContextResolver;
import com.audin.motivora.service.AuthService;
import com.audin.motivora.service.EmailVerificationService;
import com.audin.motivora.service.mails.WelcomeMail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping(path = "auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final WelcomeMail mailer;
    private final DeviceContextResolver deviceContextResolver;
    private final EmailVerificationService emailVerificationService;
    private final com.audin.motivora.config.JwtProperties jwtProperties;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping(path = "register")
    public ResponseEntity<AuthDTOResponse> register(
            @RequestBody @Valid RegisterDTORequest dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        Role role = new Role();
        role.setName(UserRole.USER);
        log.info("Registration attempt for {}", dto.getEmail());

        User user = this.authService.register(dto, role);
        this.mailer.welcome(user);
        this.emailVerificationService.sendVerificationCode(user.getEmail());

        TokenPairResponse tokens = this.jwtService.generateToken(
                user.getEmail(), this.deviceContextResolver.resolve(request));
        this.attachRefreshCookie(response, tokens.refreshToken());

        return new ResponseEntity<>(
                this.authMapper.authResponse(tokens, this.authMapper.toDto(user)),
                HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<AuthDTOResponse> login(
            @RequestBody @Valid LoginDTORequest dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        if (!authentication.isAuthenticated()) {
            throw new SecurityException("Invalid credentials");
        }

        TokenPairResponse tokens = this.jwtService.generateToken(
                dto.getEmail(), this.deviceContextResolver.resolve(request));
        this.attachRefreshCookie(response, tokens.refreshToken());

        User user = this.authService.findByEmail(dto.getEmail());
        return ResponseEntity.ok(this.authMapper.authResponse(tokens, this.authMapper.toDto(user)));
    }

    /**
     * Accepts the refresh token from the JSON body (mobile: stored in the OS keychain)
     * or from the HttpOnly cookie (web). The body wins when both are present.
     */
    @PostMapping(path = "refresh-token")
    public ResponseEntity<TokenPairResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            @CookieValue(name = REFRESH_COOKIE, required = false) String cookieToken,
            HttpServletRequest request,
            HttpServletResponse response) {

        String presented = (body != null && body.getRefreshToken() != null && !body.getRefreshToken().isBlank())
                ? body.getRefreshToken()
                : cookieToken;

        DeviceContext device = this.deviceContextResolver.resolve(request);
        TokenPairResponse tokens = this.jwtService.refreshToken(presented, device);
        this.attachRefreshCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(tokens);
    }

    private void attachRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(this.cookieSecure)
                .path("/")
                .maxAge(this.jwtProperties.getRefreshTokenTtl())
                .sameSite(this.cookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
