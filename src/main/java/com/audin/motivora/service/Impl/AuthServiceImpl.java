package com.audin.motivora.service.Impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.audin.motivora.dto.request.RegisterDTORequest;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserStatus;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.repository.RoleRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.service.AuthService;

import jakarta.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    @Override
    public User register(RegisterDTORequest dto, Role role) {
        User user = this.authMapper.registerEntity(dto);
        // Log des info de l'entity
            log.info("Registering user: {} | {} | {}", user.getPseudo(), user.getEmail(), role.getName());
        role = this.roleRepository.findByName(role.getName()).orElseThrow(() -> new EntityExistsException("Role not found"));
        if(!this.userRepository.findByEmail(user.getEmail()).isPresent()){
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);
            user = this.userRepository.save(user);

        }else{
            throw new EntityExistsException("This user already exist on database");
        }
        return user;
    }

    @Override
    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.findByEmail(username);
    }

}
