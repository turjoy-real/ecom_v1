package com.services.userservice.controllers;

import com.services.userservice.dtos.CreateRoleRequestDto;
import com.services.userservice.dtos.UserRoleRequest;
import com.services.userservice.models.Role;
import com.services.userservice.services.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequestDto requestDto) {
        Role role = roleService.createRole(requestDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@NotBlank @PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRoleToUser(@Valid @RequestBody UserRoleRequest request) {
        roleService.addRoleToUser(request.getUserEmail(), request.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/user/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRoleFromUser(@Valid @RequestBody UserRoleRequest request) {
        roleService.removeRoleFromUser(request.getUserEmail(), request.getRoleName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getUserRoles(@NotBlank @Email @PathVariable String email) {
        List<Role> roles = roleService.getUserRoles(email);
        return ResponseEntity.ok(roles);
    }
}