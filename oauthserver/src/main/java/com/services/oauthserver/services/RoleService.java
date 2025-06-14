package com.services.oauthserver.services;

import com.services.oauthserver.models.Role;
import com.services.oauthserver.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Role createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role with name " + name + " not found"));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void deleteRole(String name) {
        Role role = getRoleByName(name);
        roleRepository.delete(role);
    }
}