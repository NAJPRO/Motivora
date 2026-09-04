package com.audin.motivora.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Jwt;

@Repository
public interface JwtRepository extends CrudRepository<Jwt, Integer> {

    void deleteByExpire(boolean expire);

    @Query("FROM Jwt j WHERE j.user.email = :email AND j.expire = false")
    List<Jwt> findAllByUserEmail(@Param("email") String email);

    /**
     * Valid sessions opened from one device. A new login only revokes these, so the
     * user's other devices stay signed in.
     */
    @Query("FROM Jwt j WHERE j.user.email = :email AND j.deviceId = :deviceId AND j.expire = false")
    List<Jwt> findAllByUserEmailAndDeviceId(@Param("email") String email, @Param("deviceId") String deviceId);

    /** Sessions that were opened without a device identifier (legacy / plain web clients). */
    @Query("FROM Jwt j WHERE j.user.email = :email AND j.deviceId IS NULL AND j.expire = false")
    List<Jwt> findAllByUserEmailWithoutDevice(@Param("email") String email);

    @Query("FROM Jwt j where j.expire = false AND j.refreshToken.token = :refreshToken")
    Optional<Jwt> findByRefreshToken(@Param("refreshToken") String refreshToken);

    /** Any session carrying this refresh token, revoked ones included: used to detect replay. */
    @Query("FROM Jwt j WHERE j.refreshToken.token = :refreshToken")
    Optional<Jwt> findAnyByRefreshToken(@Param("refreshToken") String refreshToken);

    @Query("FROM Jwt j WHERE j.expire = :expire AND j.user.email = :email")
    Optional<Jwt> findByUserValidToken(@Param("email") String email, @Param("expire") boolean expire);

    @Query("FROM Jwt j WHERE j.expire = :expire AND j.token = :token")
    Optional<Jwt> findByToken(@Param("token") String token, @Param("expire") boolean expire);

    /**
     * Sessions that are revoked or past their expiry. Loaded as entities rather than
     * bulk-deleted so the cascade to {@code RefreshToken} runs and leaves no orphan rows.
     */
    @Query("FROM Jwt j WHERE j.expire = true OR (j.expireAt IS NOT NULL AND j.expireAt < :now)")
    List<Jwt> findRevokedAndExpired(@Param("now") Instant now);
}
