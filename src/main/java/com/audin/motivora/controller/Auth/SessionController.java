package com.audin.motivora.controller.Auth;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.SessionResponse;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.security.JwtService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Session lifecycle for the authenticated user: sign out of this device, of all
 * devices, and list the devices currently signed in.
 */
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final JwtService jwtService;
    private final AuthUtil authUtil;

    @PostMapping("logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        this.jwtService.logout(this.bearerToken(request));
        return ResponseEntity.ok(MessageResponse.of("Logout successfully"));
    }

    @PostMapping("logout-all")
    public ResponseEntity<MessageResponse> logoutAll() {
        this.jwtService.logoutAllDevices();
        return ResponseEntity.ok(MessageResponse.of("All sessions have been closed"));
    }

    @GetMapping("sessions")
    public ResponseEntity<List<SessionResponse>> sessions(HttpServletRequest request) {
        return ResponseEntity.ok(
                this.jwtService.listSessions(this.authUtil.getCurrentUser(), this.bearerToken(request)));
    }

    @DeleteMapping("sessions/{id}")
    public ResponseEntity<MessageResponse> revokeSession(@PathVariable Integer id) {
        this.jwtService.revokeSession(this.authUtil.getCurrentUser(), id);
        return ResponseEntity.ok(MessageResponse.of("Session closed"));
    }

    private String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("Missing bearer token");
        }
        return header.substring(7);
    }
}
