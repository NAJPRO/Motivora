package com.audin.motivora.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The authentication contract a mobile client depends on: tokens in the JSON body, several
 * devices signed in at once, refresh rotation, and 401 (never 400) when a token is stale.
 */
class AuthContractIntegrationTest extends AbstractIntegrationTest {

    private static final String PHONE = "phone-install-1";
    private static final String TABLET = "tablet-install-2";

    private String email;

    @BeforeEach
    void setUp() {
        this.email = "mobile-" + System.nanoTime() + "@example.com";
    }

    @Test
    @DisplayName("register returns both tokens in the body, not only in a cookie")
    void registerReturnsTokensInBody() throws Exception {
        MvcResult result = this.register(this.email, PHONE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

        // A React Native client has no usable cookie jar; the body must carry everything.
        assertThat(this.json(result).get("refreshToken").asText()).isNotBlank();
    }

    @Test
    @DisplayName("the refresh token is accepted from the JSON body, with no cookie involved")
    void refreshAcceptsTokenFromBody() throws Exception {
        String refreshToken = this.json(this.register(this.email, PHONE).andReturn())
                .get("refreshToken").asText();

        this.refresh(refreshToken, PHONE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("a rotated refresh token cannot be replayed, and the failure is a 401")
    void replayedRefreshTokenIsRejectedWith401() throws Exception {
        String refreshToken = this.json(this.register(this.email, PHONE).andReturn())
                .get("refreshToken").asText();

        this.refresh(refreshToken, PHONE).andExpect(status().isOk());

        // Same token a second time: rotation must have invalidated it.
        this.refresh(refreshToken, PHONE)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("signing in on a second device does not sign the first one out")
    void twoDevicesStaySignedInSimultaneously() throws Exception {
        String phoneToken = this.json(this.register(this.email, PHONE).andReturn())
                .get("accessToken").asText();

        this.mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", TABLET)
                        .header("X-Client-Platform", "android")
                        .content(this.objectMapper.writeValueAsString(Map.of(
                                "email", this.email, "password", "Password1"))))
                .andExpect(status().isOk());

        // The phone's access token must still work after the tablet signed in.
        this.mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + phoneToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(this.email));
    }

    @Test
    @DisplayName("a protected endpoint without a token returns the shared error shape")
    void protectedEndpointWithoutTokenReturnsApiError() throws Exception {
        this.mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("validation failures name the offending fields")
    void validationErrorsAreFieldScoped() throws Exception {
        this.mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(Map.of(
                                "first_name", "A",
                                "last_name", "Tester",
                                "email", "not-an-email",
                                "password", "weak"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.password").isNotEmpty());
    }

    private ResultActions register(String email, String deviceId) throws Exception {
        return this.mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Device-Id", deviceId)
                .header("X-Client-Platform", "ios")
                .content(this.objectMapper.writeValueAsString(Map.of(
                        "first_name", "Mobile",
                        "last_name", "Tester",
                        "email", email,
                        "password", "Password1"))));
    }

    private ResultActions refresh(String refreshToken, String deviceId) throws Exception {
        return this.mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Device-Id", deviceId)
                .content(this.objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))));
    }

    private JsonNode json(MvcResult result) throws Exception {
        return this.objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
