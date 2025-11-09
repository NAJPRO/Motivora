package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.UniqueField;
import com.audin.motivora.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTORequest{
    @NotBlank(message = "Pseudo is required")
    @Size(min = 2, max = 30, message = "Pseudo must be between 2 and 30 characters")
    private String pseudo;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @UniqueField(entity = User.class, fieldName = "email", message = "Email already used")
    private String email; 

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be contains 8 characters")
    private String password;
}

