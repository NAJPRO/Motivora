package com.audin.motivora.service;

public interface ResetPasswordService {
    void sendResetCode(String email);
    void validResetPassword(String email, String otpCode, String newPassword);
}
