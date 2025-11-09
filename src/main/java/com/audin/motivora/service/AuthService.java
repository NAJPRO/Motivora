package com.audin.motivora.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;

public interface AuthService extends UserDetailsService{
    User findByEmail(String email);

    User register(RegisterDTORequest dto, Role role);
}
