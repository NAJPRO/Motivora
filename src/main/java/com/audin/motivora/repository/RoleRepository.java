package com.audin.motivora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.audin.motivora.entity.Role;
import com.audin.motivora.enums.UserRole;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(Enum<UserRole> name);
}
