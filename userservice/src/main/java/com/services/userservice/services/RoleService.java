package com.services.userservice.services;

import com.services.userservice.models.Role;
import com.services.userservice.models.UserRole;
import com.services.userservice.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public void initializeRoles() {
        Arrays.stream(UserRole.values())
                .filter(role -> !roleRepository.existsByName(role))
                .forEach(role -> {
                    Role newRole = new Role();
                    newRole.setName(role);
                    roleRepository.save(newRole);
                });
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(UserRole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }
}