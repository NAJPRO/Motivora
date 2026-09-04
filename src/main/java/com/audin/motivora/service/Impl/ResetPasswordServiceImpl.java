package com.audin.motivora.service.Impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import com.audin.motivora.security.JwtService;
import com.audin.motivora.service.ResetPasswordService;
import com.audin.motivora.utils.OtpGenerator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRATION_TIME = 10; // en minutes

    private final NotificationService notificationService;
    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void sendResetCode(String email) {
        String otpCode = OtpGenerator.generateOtp(OTP_LENGTH);

        this.saveOtpCode(otpCode, email);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("otpCode", otpCode);
        emailData.put("expirationTime", EXPIRATION_TIME);
        emailData.put("userEmail", email);

        notificationService.sendNotification(
                email,
                NotificationTemplateType.PASSWORD_RESET,
                emailData);
    }

    @Override
    @Transactional
    public void validResetPassword(String email, String otpCode, String newPassword) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        OtpCode otpCodeEntity = this.otpCodeRepository
                .findByOtpAndUserEmailAndPurpose(otpCode, email, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException("OTP_INVALID", "Invalid OTP code"));

        if (otpCodeEntity.getConfirmedAt() != null) {
            throw new BusinessException("OTP_ALREADY_USED", "This OTP code has already been used");
        }
        if (otpCodeEntity.getExpiresAt() == null || otpCodeEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("OTP_EXPIRED", "This OTP code has expired");
        }

        user.setPassword(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);

        // A password reset must invalidate sessions opened with the old credentials.
        this.jwtService.disableTokens(user);

        otpCodeEntity.setConfirmedAt(Instant.now());
        this.otpCodeRepository.save(otpCodeEntity);

        // Invalidate any remaining reset codes for this user.
        this.otpCodeRepository.deleteByUserIdAndPurpose(user.getId(), OtpPurpose.PASSWORD_RESET);
    }

    private void saveOtpCode(String otpCode, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Only one active reset code per user at a time.
        this.otpCodeRepository.deleteByUserIdAndPurpose(user.getId(), OtpPurpose.PASSWORD_RESET);

        Instant now = Instant.now();
        OtpCode otpCodeEntity = new OtpCode();
        otpCodeEntity.setOtp(otpCode);
        otpCodeEntity.setUser(user);
        otpCodeEntity.setCreatedAt(now);
        otpCodeEntity.setExpiresAt(now.plusSeconds(EXPIRATION_TIME * 60L));
        otpCodeEntity.setPurpose(OtpPurpose.PASSWORD_RESET);

        otpCodeRepository.save(otpCodeEntity);
    }

}
