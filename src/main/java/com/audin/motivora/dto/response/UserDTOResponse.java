package com.audin.motivora.dto.response;

import com.audin.motivora.entity.Role;
import com.audin.motivora.enums.UserStatus;

public record UserDTOResponse(
    Integer id, 
    String pseudo,  
    String email,
    UserStatus status,
    Role role
) {}
