package com.audin.motivora.service;

import java.util.List;

import com.audin.motivora.dto.request.RegisterDeviceRequest;
import com.audin.motivora.dto.response.DeviceTokenResponse;

public interface DeviceTokenService {

    /** Registers or refreshes the caller's push token. Idempotent: the app calls it on every start. */
    DeviceTokenResponse register(RegisterDeviceRequest request);

    List<DeviceTokenResponse> listMyDevices();

    void unregister(String token);

    /** Disables tokens the provider reported as undeliverable. */
    void disableTokens(List<String> tokens);
}
