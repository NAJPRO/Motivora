package com.audin.motivora.dto.response;

public record AuthDTOResponse(
    String token,
    UserDTOResponse data
) {

}
