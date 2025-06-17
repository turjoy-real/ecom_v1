package com.services.userservice.controllers;

import com.services.userservice.dtos.CreateRoleRequestDto;
import com.services.userservice.dtos.UserRoleRequest;
import com.services.userservice.models.Role;
import com.services.userservice.services.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/user/add")
    public ResponseEntity<Void> addRoleToUser(@Valid @RequestBody UserRoleRequest request) {
        roleService.addRoleToUser(request.getUserEmail(), request.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/remove")
    public ResponseEntity<Void> removeRoleFromUser(@Valid @RequestBody UserRoleRequest request) {
        roleService.removeRoleFromUser(request.getUserEmail(), request.getRoleName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable String email) {
        List<Role> roles = roleService.getUserRoles(email);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/current")
    public ResponseEntity<List<Role>> getCurrentUserRoles(Authentication authentication) {
        String email = authentication.getName();
        List<Role> roles = roleService.getUserRoles(email);
        return ResponseEntity.ok(roles);
    }
}