package com.audin.motivora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Account deletion is irreversible, so it is confirmed with the account password —
 * a stolen unlocked phone must not be enough to wipe someone's account.
 */
@Getter
@Setter
public class DeleteAccountRequest {

    @NotBlank(message = "Password confirmation is required")
    private String password;
}
