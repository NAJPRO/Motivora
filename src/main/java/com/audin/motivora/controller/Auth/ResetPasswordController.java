package com.audin.motivora.controller.Auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.ResetPasswordConfirmRequest;
import com.audin.motivora.dto.request.ResetPasswordDTORequest;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.service.ResetPasswordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "reset-password")
@Validated
@RequiredArgsConstructor
public class ResetPasswordController {
    private final ResetPasswordService resetPasswordService;

    @PostMapping("request-otp")
    public ResponseEntity<MessageResponse> sendOtpCode(@RequestBody @Valid ResetPasswordDTORequest dto) {
        this.resetPasswordService.sendResetCode(dto.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
                "An OTP code has been sent to you. Verify your email account"));
    }

    @PostMapping("confirm")
    public ResponseEntity<MessageResponse> confirm(@RequestBody @Valid ResetPasswordConfirmRequest dto) {
        this.resetPasswordService.validResetPassword(dto.getEmail(), dto.getOtp(), dto.getNewPassword());
        return ResponseEntity.ok(MessageResponse.of("Password reset successfully"));
    }

}
