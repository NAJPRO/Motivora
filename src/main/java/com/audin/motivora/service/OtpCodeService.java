package com.audin.motivora.service;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.entity.User;

public interface OtpCodeService {
    public void save(OtpCode otpCode);
    public OtpCode showValidation(User user);
    public void confirmOtp(OtpCode otpValidation);

}