package com.audin.motivora.controller.Client;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.ChangePasswordRequest;
import com.audin.motivora.dto.request.DeleteAccountRequest;
import com.audin.motivora.dto.request.UpdateProfileRequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * The signed-in user's own account.
 */
@RestController
@RequestMapping("me")
@RequiredArgsConstructor
@Validated
public class MeController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<UserDTOResponse> me() {
        return ResponseEntity.ok(this.accountService.getProfile());
    }

    @PutMapping
    public ResponseEntity<UserDTOResponse> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(this.accountService.updateProfile(request));
    }

    @PostMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        this.accountService.changePassword(request);
        return ResponseEntity.ok(MessageResponse.of(
                "Password updated. Please sign in again on your devices."));
    }

    /**
     * Account deletion, required by App Store guideline 5.1.1(v). Personal data is
     * anonymised and every session is closed.
     */
    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteAccount(@RequestBody @Valid DeleteAccountRequest request) {
        this.accountService.deleteOwnAccount(request.getPassword());
        return ResponseEntity.ok(MessageResponse.of("Your account has been deleted"));
    }
}
