package com.audin.motivora.mapper.Impl;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.AuthDTOResponse;
import com.audin.motivora.dto.response.TokenPairResponse;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.User;
import com.audin.motivora.mapper.AuthMapper;

@Component
public class AuthMapperImpl implements AuthMapper{

    @Override
    public AuthDTOResponse authResponse(TokenPairResponse tokens, UserDTOResponse dto) {
        return new AuthDTOResponse(
            tokens.accessToken(),
            tokens.accessToken(),
            tokens.refreshToken(),
            tokens.tokenType(),
            tokens.expiresIn(),
            dto
        );
    }

    @Override
    public UserDTOResponse toDto(User entity) {
        return new UserDTOResponse(
            entity.getId(),
            entity.getPseudo(),
            entity.getEmail(),
            entity.getAvatarUrl(),
            entity.getStatus(),
            entity.getRole() != null ? entity.getRole().getName().name() : null,
            entity.getEmailVerifiedAt() != null,
            entity.getEmailVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    @Override
    public User registerEntity(RegisterDTORequest dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPseudo(dto.getFirst_name() + " " + dto.getLast_name());
        user.setPassword(dto.getPassword());
        return user;
    }

}
