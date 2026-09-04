package com.audin.motivora.utils.seeders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.enums.UserStatus;
import com.audin.motivora.repository.RoleRepository;
import com.audin.motivora.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bootstraps a single ADMIN account at startup when both
 * {@code app.admin.email} and {@code app.admin.password} are configured and the
 * account does not yet exist. Runs after {@link RoleSeeder} so the ADMIN role is
 * available.
 */
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.info("Admin bootstrap skipped: app.admin.email/password not configured");
            return;
        }
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin bootstrap skipped: user {} already exists", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(UserRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not seeded"));

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPseudo("Administrator");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        log.info("Admin user created: {}", adminEmail);
    }
}
