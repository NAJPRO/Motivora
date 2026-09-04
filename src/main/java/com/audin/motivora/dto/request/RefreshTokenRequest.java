package com.audin.motivora.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Body accepted by {@code POST /auth/refresh-token}. Optional: a web client can keep
 * relying on the HttpOnly cookie and send an empty body.
 */
@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken;
}
