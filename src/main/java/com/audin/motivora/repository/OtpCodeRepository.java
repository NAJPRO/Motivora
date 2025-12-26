package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.audin.motivora.entity.OtpCode;

public interface OtpCodeRepository extends CrudRepository<OtpCode, Integer> {
    Optional<OtpCode> findByOtpAndUserEmail(String code, String email);
}
