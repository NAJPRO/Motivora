package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.ExistField;
import com.audin.motivora.dto.Annotation.UniqueField;
import com.audin.motivora.entity.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTORequest
{
    @NotBlank(message = "Email required")
    @ExistField(entity = User.class, fieldName = "email", message = "Account don't exist")
    private String email;

    @NotBlank(message = "Password required")
    private String password;

}
