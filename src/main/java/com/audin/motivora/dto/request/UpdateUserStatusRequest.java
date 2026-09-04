package com.audin.motivora.dto.request;

import com.audin.motivora.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
