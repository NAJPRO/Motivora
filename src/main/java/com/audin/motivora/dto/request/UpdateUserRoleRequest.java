package com.audin.motivora.dto.request;

import com.audin.motivora.enums.UserRole;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;
}
