package com.audin.motivora.mapper;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.AuthDTOResponse;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.User;

public interface AuthMapper {
    User registerEntity(RegisterDTORequest dto);
    UserDTOResponse toDto(User entity);
    AuthDTOResponse authResponse(String token, UserDTOResponse dto);
}
