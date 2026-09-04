package com.audin.motivora.mapper.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.enums.UserStatus;

class AuthMapperImplTest {

    private final AuthMapperImpl mapper = new AuthMapperImpl();

    @Test
    void toDtoExposesRoleAsStringNotEntity() {
        Role role = new Role();
        role.setName(UserRole.ADMIN);
        User user = new User();
        user.setId(1);
        user.setPseudo("Alice");
        user.setEmail("alice@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);

        UserDTOResponse dto = mapper.toDto(user);

        assertThat(dto.role()).isEqualTo("ADMIN");
        assertThat(dto.email()).isEqualTo("alice@example.com");
        assertThat(dto.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void registerEntityBuildsPseudoFromNames() {
        RegisterDTORequest request = new RegisterDTORequest();
        request.setFirst_name("Jean");
        request.setLast_name("Dupont");
        request.setEmail("jean@example.com");
        request.setPassword("Passw0rd");

        User user = mapper.registerEntity(request);

        assertThat(user.getPseudo()).isEqualTo("Jean Dupont");
        assertThat(user.getEmail()).isEqualTo("jean@example.com");
        assertThat(user.getPassword()).isEqualTo("Passw0rd");
    }
}
