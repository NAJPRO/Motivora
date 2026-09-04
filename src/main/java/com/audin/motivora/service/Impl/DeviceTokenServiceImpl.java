package com.audin.motivora.service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.request.RegisterDeviceRequest;
import com.audin.motivora.dto.response.DeviceTokenResponse;
import com.audin.motivora.entity.DeviceToken;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.DevicePlatform;
import com.audin.motivora.repository.DeviceTokenRepository;
import com.audin.motivora.service.DeviceTokenService;
import com.audin.motivora.utils.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final AuthUtil authUtil;

    /**
     * A push token can migrate between accounts on a shared device, so an existing row is
     * reassigned to the current user rather than rejected as a duplicate.
     */
    @Override
    @Transactional
    public DeviceTokenResponse register(RegisterDeviceRequest request) {
        User currentUser = this.authUtil.getCurrentUser();

        DeviceToken deviceToken = this.deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(() -> DeviceToken.builder().token(request.getToken()).build());

        deviceToken.setUser(currentUser);
        deviceToken.setPlatform(DevicePlatform.from(request.getPlatform()));
        deviceToken.setDeviceId(request.getDeviceId());
        deviceToken.setDeviceName(request.getDeviceName());
        deviceToken.setEnabled(true);
        deviceToken.setLastUsedAt(LocalDateTime.now());

        return this.toResponse(this.deviceTokenRepository.save(deviceToken));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTokenResponse> listMyDevices() {
        return this.deviceTokenRepository.findAllByUserId(this.authUtil.getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unregister(String token) {
        User currentUser = this.authUtil.getCurrentUser();
        this.deviceTokenRepository.findByToken(token)
                .filter(device -> device.getUser().getId().equals(currentUser.getId()))
                .ifPresent(this.deviceTokenRepository::delete);
    }

    @Override
    @Transactional
    public void disableTokens(List<String> tokens) {
        if (tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> this.deviceTokenRepository.findByToken(token).ifPresent(device -> {
            device.setEnabled(false);
            this.deviceTokenRepository.save(device);
        }));
        log.info("Disabled {} undeliverable push token(s)", tokens.size());
    }

    private DeviceTokenResponse toResponse(DeviceToken deviceToken) {
        return new DeviceTokenResponse(
                deviceToken.getId(),
                deviceToken.getPlatform() != null ? deviceToken.getPlatform().name() : null,
                deviceToken.getDeviceName(),
                deviceToken.isEnabled(),
                deviceToken.getCreatedAt());
    }
}
