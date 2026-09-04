package com.audin.motivora.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.audin.motivora.dto.request.AdminCreateUserRequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.enums.UserStatus;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.repository.RoleRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.security.JwtService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock AuthMapper authMapper;
    @Mock JwtService jwtService;
    @Mock AuthUtil authUtil;

    @InjectMocks UserServiceImpl service;

    private User user(int id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private UserDTOResponse anyDto() {
        return new UserDTOResponse(1, "p", "e@x.com", null, UserStatus.ACTIVE, "USER", false, null, null);
    }

    @Test
    void createEncodesPasswordAssignsRoleAndActivates() {
        AdminCreateUserRequest dto = new AdminCreateUserRequest();
        dto.setPseudo("Mod");
        dto.setEmail("mod@x.com");
        dto.setPassword("Passw0rd");
        dto.setRole(UserRole.MODERATOR);

        Role role = new Role();
        role.setName(UserRole.MODERATOR);

        when(roleRepository.findByName(UserRole.MODERATOR)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Passw0rd")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authMapper.toDto(any(User.class))).thenReturn(anyDto());

        service.create(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getRole()).isSameAs(role);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getEmail()).isEqualTo("mod@x.com");
    }

    @Test
    void updateRoleChangesRoleAndInvalidatesSessions() {
        User target = user(2);
        Role role = new Role();
        role.setName(UserRole.ADMIN);
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(authUtil.getCurrentUser()).thenReturn(user(1));
        when(roleRepository.findByName(UserRole.ADMIN)).thenReturn(Optional.of(role));
        when(userRepository.save(target)).thenReturn(target);
        when(authMapper.toDto(target)).thenReturn(anyDto());

        service.updateRole(2, UserRole.ADMIN);

        assertThat(target.getRole()).isSameAs(role);
        verify(jwtService).disableTokens(target);
    }

    @Test
    void updateRoleOnSelfIsRejected() {
        User self = user(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(self));
        when(authUtil.getCurrentUser()).thenReturn(self);

        assertThatThrownBy(() -> service.updateRole(1, UserRole.USER))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).disableTokens(any());
    }

    @Test
    void updateStatusToNonActiveDisablesTokens() {
        User target = user(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(authUtil.getCurrentUser()).thenReturn(user(1));
        when(userRepository.save(target)).thenReturn(target);
        when(authMapper.toDto(target)).thenReturn(anyDto());

        service.updateStatus(2, UserStatus.SUSPENDED);

        assertThat(target.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(jwtService).disableTokens(target);
    }

    @Test
    void updateStatusToActiveKeepsSessions() {
        User target = user(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(authUtil.getCurrentUser()).thenReturn(user(1));
        when(userRepository.save(target)).thenReturn(target);
        when(authMapper.toDto(target)).thenReturn(anyDto());

        service.updateStatus(2, UserStatus.ACTIVE);

        verify(jwtService, never()).disableTokens(any());
    }

    @Test
    void deleteSoftDeletesAndInvalidatesSessions() {
        User target = user(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(target));
        when(authUtil.getCurrentUser()).thenReturn(user(1));

        service.delete(2);

        assertThat(target.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository).save(target);
        verify(jwtService).disableTokens(target);
    }

    @Test
    void deleteSelfIsRejected() {
        User self = user(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(self));
        when(authUtil.getCurrentUser()).thenReturn(self);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(userRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
