package com.audin.motivora.mapper.Impl;

import org.springframework.stereotype.Component;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.AuthDTOResponse;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.User;
import com.audin.motivora.mapper.AuthMapper;

@Component
public class AuthMapperImpl implements AuthMapper{

    @Override
    public AuthDTOResponse authResponse(String token, UserDTOResponse dto) {
        return new AuthDTOResponse(
            token,
            dto
        );
    }

    @Override
    public UserDTOResponse toDto(User entity) {
        return new UserDTOResponse(
            entity.getId(),
            entity.getPseudo(),
            entity.getEmail(),
            entity.getStatus(),
            entity.getRole()
        );
    }

    @Override
    public User registerEntity(RegisterDTORequest dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPseudo(dto.getPseudo());
        user.setPassword(dto.getPassword());
        return user;
    }

}
