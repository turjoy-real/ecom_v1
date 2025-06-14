package com.services.oauthserver.controllers;

import com.services.oauthserver.dtos.CreateRoleRequestDto;
import com.services.oauthserver.models.Role;
import com.services.oauthserver.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequestDto requestDto) {
        Role role = roleService.createRole(requestDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/debug")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> debug(Authentication authentication) {
        return ResponseEntity.ok(authentication.getAuthorities());
    }
}