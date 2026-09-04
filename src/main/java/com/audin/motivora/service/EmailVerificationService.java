package com.audin.motivora.service;

public interface EmailVerificationService {

    /** Issues a code and emails it. Silent when the address is unknown, to avoid enumeration. */
    void sendVerificationCode(String email);

    void verify(String email, String otp);
}
