package com.audin.motivora.exception;

import lombok.Getter;

/**
 * Domain-level failure, rendered as HTTP 400.
 *
 * The optional {@code errorCode} is the stable identifier a client branches on
 * (e.g. {@code OTP_EXPIRED}); the message stays human-readable and free to reword.
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final String DEFAULT_CODE = "BUSINESS_ERROR";

    private final String errorCode;

    public BusinessException(String message) {
        this(DEFAULT_CODE, message);
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
