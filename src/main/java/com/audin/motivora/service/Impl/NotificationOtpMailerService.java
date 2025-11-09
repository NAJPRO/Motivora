package com.audin.motivora.service.Impl;

import org.springframework.stereotype.Service;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.service.OtpCodeService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationOtpMailerService implements OtpCodeService{


    @Override
    public void save(com.audin.motivora.entity.User user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public OtpCode showValidation(com.audin.motivora.entity.User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void confirmOtp(OtpCode otpValidation) {
        // TODO Auto-generated method stub
        
    }
}
