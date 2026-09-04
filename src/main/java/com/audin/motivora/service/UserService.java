package com.audin.motivora.service;

import org.springframework.data.domain.Page;

import com.audin.motivora.dto.request.AdminCreateUserRequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.enums.UserStatus;

public interface UserService {
    Page<UserDTOResponse> getAll(int page, int size, String search);
    UserDTOResponse getById(Integer id);
    UserDTOResponse create(AdminCreateUserRequest dto);
    UserDTOResponse updateRole(Integer id, UserRole role);
    UserDTOResponse updateStatus(Integer id, UserStatus status);
    void delete(Integer id);
}
