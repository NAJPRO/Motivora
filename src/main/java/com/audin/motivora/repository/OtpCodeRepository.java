package com.audin.motivora.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.OtpCode;
import com.audin.motivora.enums.OtpPurpose;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Integer> {

    Optional<OtpCode> findByOtpAndUserEmailAndPurpose(String otp, String email, OtpPurpose purpose);

    void deleteByUserIdAndPurpose(Integer userId, OtpPurpose purpose);

    void deleteByUserId(Integer userId);

    List<OtpCode> findAllByExpiresAtBefore(Instant cutoff);
}
