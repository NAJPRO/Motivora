package com.audin.motivora.enums;

/**
 * What a one-time code was issued for. Scoping codes by purpose lets a user verify their
 * email and reset their password without one flow invalidating the other's code.
 */
public enum OtpPurpose {
    PASSWORD_RESET,
    EMAIL_VERIFICATION
}
