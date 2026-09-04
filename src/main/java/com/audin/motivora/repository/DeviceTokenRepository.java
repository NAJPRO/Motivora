package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findAllByUserIdAndEnabledTrue(Integer userId);

    List<DeviceToken> findAllByUserId(Integer userId);

    void deleteByUserId(Integer userId);
}
