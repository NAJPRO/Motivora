package com.audin.motivora.utils.seeders;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.audin.motivora.entity.Role;
import com.audin.motivora.enums.UserRole;
import com.audin.motivora.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {
 private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // On boucle sur toutes les valeurs de l’enum
        for (UserRole userRole : UserRole.values()) {
            roleRepository.findByName(userRole).ifPresentOrElse(
                role -> log.info("Role {} déjà existant", userRole),
                () -> {
                    Role newRole = new Role();
                    newRole.setName(userRole);
                    roleRepository.save(newRole);
                    log.info("Role ajouté : {}", userRole);
                }
            );
        }
    }
}
