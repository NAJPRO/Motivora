package com.audin.motivora.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.audin.motivora.service.UserService;
import com.audin.motivora.utils.AuthUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtService jwtService;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTOResponse> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.search(term, pageable).map(authMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTOResponse getById(Integer id) {
        return authMapper.toDto(this.findUser(id));
    }

    @Override
    @Transactional
    public UserDTOResponse create(AdminCreateUserRequest dto) {
        Role role = this.findRole(dto.getRole());

        User user = new User();
        user.setPseudo(dto.getPseudo());
        user.setEmail(dto.getEmail());
        user.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        return authMapper.toDto(this.userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTOResponse updateRole(Integer id, UserRole role) {
        User user = this.findUser(id);
        this.assertNotSelf(user, "You cannot change your own role");

        user.setRole(this.findRole(role));
        user = this.userRepository.save(user);

        // Privilege change: invalidate existing sessions so a new token is issued.
        this.jwtService.disableTokens(user);

        return authMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDTOResponse updateStatus(Integer id, UserStatus status) {
        User user = this.findUser(id);
        this.assertNotSelf(user, "You cannot change your own status");

        user.setStatus(status);
        user = this.userRepository.save(user);

        // A non-active account must not keep working sessions.
        if (status != UserStatus.ACTIVE) {
            this.jwtService.disableTokens(user);
        }

        return authMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        User user = this.findUser(id);
        this.assertNotSelf(user, "You cannot delete your own account");

        // Soft delete: preserves referential integrity (quotes, favorites, etc.).
        user.setStatus(UserStatus.DELETED);
        this.userRepository.save(user);
        this.jwtService.disableTokens(user);
    }

    private User findUser(Integer id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Role findRole(UserRole role) {
        return this.roleRepository.findByName(role)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + role));
    }

    private void assertNotSelf(User target, String message) {
        User current = this.authUtil.getCurrentUser();
        if (target.getId().equals(current.getId())) {
            throw new BusinessException(message);
        }
    }
}
