package com.audin.motivora.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OtpGeneratorTest {

    @Test
    void generatesNumericCodeOfRequestedLength() {
        String otp = OtpGenerator.generateOtp(6);
        assertThat(otp).hasSize(6).matches("\\d{6}");
    }

    @Test
    void rejectsNonPositiveLength() {
        assertThatThrownBy(() -> OtpGenerator.generateOtp(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTooLargeLength() {
        assertThatThrownBy(() -> OtpGenerator.generateOtp(19))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
