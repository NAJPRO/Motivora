package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.ExistField;
import com.audin.motivora.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTORequest {
    @NotBlank(message = "Email required")
    @Email(message = "Email is not validated")
    @ExistField(entity = User.class, fieldName = "email", message = "Account don't exist")
    private String email;
}
