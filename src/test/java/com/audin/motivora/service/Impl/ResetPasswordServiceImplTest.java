package com.audin.motivora.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.OtpPurpose;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.notification.api.NotificationService;
import com.audin.motivora.repository.OtpCodeRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.security.JwtService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceImplTest {

    @Mock NotificationService notificationService;
    @Mock OtpCodeRepository otpCodeRepository;
    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks ResetPasswordServiceImpl service;

    private User user() {
        User u = new User();
        u.setId(1);
        u.setEmail("a@b.com");
        return u;
    }

    private OtpCode otp(Instant expiresAt, Instant confirmedAt) {
        OtpCode otp = new OtpCode();
        otp.setOtp("123456");
        otp.setExpiresAt(expiresAt);
        otp.setConfirmedAt(confirmedAt);
        return otp;
    }

    @Test
    void validResetEncodesPasswordMarksOtpAndPurges() {
        User user = user();
        OtpCode otp = otp(Instant.now().plusSeconds(300), null);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByOtpAndUserEmailAndPurpose("123456", "a@b.com", OtpPurpose.PASSWORD_RESET)).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("NewPass1")).thenReturn("ENCODED");

        service.validResetPassword("a@b.com", "123456", "NewPass1");

        assertThat(user.getPassword()).isEqualTo("ENCODED");
        assertThat(otp.getConfirmedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(otpCodeRepository).deleteByUserIdAndPurpose(1, OtpPurpose.PASSWORD_RESET);
        // A reset must not leave sessions open that were established with the old password.
        verify(jwtService).disableTokens(user);
    }

    @Test
    void validResetFailsWhenUserMissing() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validResetPassword("a@b.com", "123456", "NewPass1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validResetFailsWhenOtpUnknown() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user()));
        when(otpCodeRepository.findByOtpAndUserEmailAndPurpose("123456", "a@b.com", OtpPurpose.PASSWORD_RESET)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validResetPassword("a@b.com", "123456", "NewPass1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validResetFailsWhenOtpAlreadyUsed() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user()));
        when(otpCodeRepository.findByOtpAndUserEmailAndPurpose("123456", "a@b.com", OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(otp(Instant.now().plusSeconds(300), Instant.now())));

        assertThatThrownBy(() -> service.validResetPassword("a@b.com", "123456", "NewPass1"))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void validResetFailsWhenOtpExpired() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user()));
        when(otpCodeRepository.findByOtpAndUserEmailAndPurpose("123456", "a@b.com", OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(otp(Instant.now().minusSeconds(10), null)));

        assertThatThrownBy(() -> service.validResetPassword("a@b.com", "123456", "NewPass1"))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any());
    }
}
