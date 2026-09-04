package com.audin.motivora.service.Impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.OtpPurpose;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.notification.api.NotificationService;
import com.audin.motivora.notification.model.NotificationTemplateType;
import com.audin.motivora.repository.OtpCodeRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.service.EmailVerificationService;
import com.audin.motivora.utils.OtpGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 30;

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        Optional<User> found = this.userRepository.findByEmail(email);
        if (found.isEmpty()) {
            // Do not disclose whether the address exists.
            log.info("Verification code requested for an unknown address");
            return;
        }

        User user = found.get();
        if (user.getEmailVerifiedAt() != null) {
            throw new BusinessException("EMAIL_ALREADY_VERIFIED", "This email address is already verified");
        }

        String code = OtpGenerator.generateOtp(OTP_LENGTH);
        this.otpCodeRepository.deleteByUserIdAndPurpose(user.getId(), OtpPurpose.EMAIL_VERIFICATION);

        Instant now = Instant.now();
        OtpCode otpCode = new OtpCode();
        otpCode.setOtp(code);
        otpCode.setUser(user);
        otpCode.setPurpose(OtpPurpose.EMAIL_VERIFICATION);
        otpCode.setCreatedAt(now);
        otpCode.setExpiresAt(now.plusSeconds(EXPIRATION_MINUTES * 60L));
        this.otpCodeRepository.save(otpCode);

        Map<String, Object> data = new HashMap<>();
        data.put("otpCode", code);
        data.put("expirationTime", EXPIRATION_MINUTES);
        data.put("userName", user.getPseudo());
        data.put("userEmail", user.getEmail());

        this.notificationService.sendNotification(
                user.getEmail(), NotificationTemplateType.EMAIL_VERIFICATION, data);
    }

    @Override
    @Transactional
    public void verify(String email, String otp) {
        OtpCode otpCode = this.otpCodeRepository
                .findByOtpAndUserEmailAndPurpose(otp, email, OtpPurpose.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BusinessException("OTP_INVALID", "Invalid verification code"));

        if (otpCode.getConfirmedAt() != null) {
            throw new BusinessException("OTP_ALREADY_USED", "This verification code has already been used");
        }
        if (otpCode.getExpiresAt() == null || otpCode.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("OTP_EXPIRED", "This verification code has expired");
        }

        User user = otpCode.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        this.userRepository.save(user);

        otpCode.setConfirmedAt(Instant.now());
        this.otpCodeRepository.save(otpCode);
        this.otpCodeRepository.deleteByUserIdAndPurpose(user.getId(), OtpPurpose.EMAIL_VERIFICATION);
    }
}
