package com.audin.motivora.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.audin.motivora.entity.Jwt;

@Repository
public interface JwtRepository extends CrudRepository<Jwt, Integer> {
    void deleteByExpire(boolean expire);

    @Query("FROM Jwt j WHERE j.user.email = :email AND j.expire = false")
    List<Jwt> findAllByUserEmail(String email);


    @Query("FROM Jwt j where j.expire = false AND j.refreshToken.token = :refreshTokenMap")
    Optional<Jwt> findByRefrechToken(String refreshTokenMap);

    @Query("FROM Jwt j WHERE j.expire = :expire AND j.user.email = :email")
    Optional<Jwt> findByUserValidToken(String email, boolean expire);

    @Query("FROM Jwt j WHERE j.expire = :expire AND j.token = :token")

    Optional<Jwt> findByToken(String token, boolean expire);


}
