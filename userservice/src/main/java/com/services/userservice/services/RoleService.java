package com.services.userservice.services;

import com.services.userservice.models.Role;
import com.services.userservice.models.User;
import com.services.userservice.repositories.RoleRepository;
import com.services.userservice.repositories.UserRepo;
import com.services.userservice.exceptions.RoleNotFound;
import com.services.userservice.exceptions.UserNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepo userRepository;

    public RoleService(RoleRepository roleRepository, UserRepo userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void addRoleToUser(String userEmail, String roleName) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + userEmail));

        Role role = getRoleByName(roleName);

        if (!user.getRoles().contains(role)) {
            List<Role> roles = user.getRoles();

            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User already has role: " + roleName);
        }
    }

    @Transactional
    public void removeRoleFromUser(String userEmail, String roleName) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + userEmail));

        Role role = getRoleByName(roleName);

        if (user.getRoles().contains(role)) {

            List<Role> roles = user.getRoles();
            roles.remove(role);

            if (roles.isEmpty()) {
                throw new IllegalArgumentException("User must have at least one role");
            }

            user.setRoles(roles);
            userRepository.save(user);
        } else {
            throw new RoleNotFound(roleName + " not found for user: " + userEmail);
        }
    }

    public List<Role> getUserRoles(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + userEmail));
        return user.getRoles();
    }
}