package com.services.userservice.repositories;

import com.services.userservice.models.Role;
import com.services.userservice.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(UserRole name);

    boolean existsByName(UserRole name);
}