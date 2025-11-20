package com.audin.motivora.service.Impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.entity.User;
import com.audin.motivora.notification.api.NotificationService;
import com.audin.motivora.notification.model.NotificationTemplateType;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.service.ResetPasswordService;
import com.audin.motivora.utils.OtpGenerator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResetPasswordServiceImpl implements ResetPasswordService{
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRATION_TIME = 10; // en minutes
    private final NotificationService notificationService;
    private final OtpCodeServiceImpl otpCodeServiceImpl;
    private final UserRepository userRepository;
    @Override
    public void sendResetCode(String email) {
        String otpCode = OtpGenerator.generateOtp(OTP_LENGTH);
        
        // Enregistrer le code OTP dans la base de données
        this.saveOtpCode(otpCode, email);


        // Préparer les données pour l'e-mail
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("otpCode", otpCode);
        emailData.put("expirationTime", EXPIRATION_TIME);
        emailData.put("userEmail", email);
        
        notificationService.sendNotification(
            email,                                 
            NotificationTemplateType.PASSWORD_RESET, 
            emailData                              
        );
    }

    private void saveOtpCode(String otpCode, String email) {
        // récupérer l'utilisateur par email (implémentation non montrée ici)
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        OtpCode otpCodeEntity = new OtpCode();
        Instant now = Instant.now();
        otpCodeEntity.setOtp(otpCode);
        otpCodeEntity.setUser(user);
        otpCodeEntity.setExpiresAt(now.plusSeconds(EXPIRATION_TIME * 60));

        otpCodeServiceImpl.save(otpCodeEntity);
    }
    
}
