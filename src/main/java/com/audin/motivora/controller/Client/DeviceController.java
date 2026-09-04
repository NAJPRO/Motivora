package com.audin.motivora.controller.Client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.RegisterDeviceRequest;
import com.audin.motivora.dto.response.DeviceTokenResponse;
import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.service.DeviceTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Push registration. The app calls {@code POST} on every start, since the provider rotates
 * tokens, and {@code DELETE} when the user turns notifications off or signs out.
 */
@RestController
@RequestMapping("me/devices")
@RequiredArgsConstructor
@Validated
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<DeviceTokenResponse> register(@RequestBody @Valid RegisterDeviceRequest request) {
        return ResponseEntity.ok(this.deviceTokenService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceTokenResponse>> index() {
        return ResponseEntity.ok(this.deviceTokenService.listMyDevices());
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<MessageResponse> unregister(@PathVariable String token) {
        this.deviceTokenService.unregister(token);
        return ResponseEntity.ok(MessageResponse.of("Device unregistered"));
    }
}
