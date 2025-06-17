package com.services.userservice.controllers;

import com.services.userservice.dtos.AddressDTO;
import com.services.userservice.dtos.CreateAddressRequest;
import com.services.userservice.dtos.UpdateAddressRequest;
import com.services.userservice.services.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressDTO>> getMyAddresses(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.getUserAddresses(email));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> createAddress(
            Authentication authentication,
            @Valid @RequestBody CreateAddressRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.createAddress(email, request));
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> getAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.getAddress(addressId, email));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(addressService.updateAddress(addressId, email, request));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        String email = authentication.getName();
        addressService.deleteAddress(addressId, email);
        return ResponseEntity.noContent().build();
    }

    // Admin endpoints
    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable String email) {
        return ResponseEntity.ok(addressService.getUserAddresses(email));
    }

    @PostMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressDTO> createAddressForUser(
            @PathVariable String email,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(email, request));
    }

    @PutMapping("/user/{email}/{addressId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressDTO> updateUserAddress(
            @PathVariable String email,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, email, request));
    }

    @DeleteMapping("/user/{email}/{addressId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserAddress(
            @PathVariable String email,
            @PathVariable Long addressId) {
        addressService.deleteAddress(addressId, email);
        return ResponseEntity.noContent().build();
    }
}