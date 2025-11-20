package com.audin.motivora.service.Impl;

import org.springframework.stereotype.Service;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.entity.User;
import com.audin.motivora.repository.OtpCodeRepository;
import com.audin.motivora.service.OtpCodeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpCodeServiceImpl implements OtpCodeService{
    private final OtpCodeRepository otpCodeRepository;
    @Override
    public void confirmOtp(OtpCode otpValidation) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void save(OtpCode otpCode) {
        otpCodeRepository.save(otpCode);
    }

    @Override
    public OtpCode showValidation(User user) {
        // TODO Auto-generated method stub
        return null;
    }

}
