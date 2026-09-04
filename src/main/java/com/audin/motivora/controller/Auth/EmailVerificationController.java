package com.audin.motivora.controller.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.ResendVerificationRequest;
import com.audin.motivora.dto.request.VerifyEmailRequest;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.service.EmailVerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("verify-email")
@RequiredArgsConstructor
@Validated
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("resend")
    public ResponseEntity<MessageResponse> resend(@RequestBody @Valid ResendVerificationRequest request) {
        this.emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
                "If this address exists, a verification code has been sent to it"));
    }

    @PostMapping("confirm")
    public ResponseEntity<MessageResponse> confirm(@RequestBody @Valid VerifyEmailRequest request) {
        this.emailVerificationService.verify(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(MessageResponse.of("Email address verified"));
    }
}
