package com.services.userservice.controllers;

import com.services.common.dtos.AddressDTO;

import com.services.userservice.repositories.UserRepo;
import com.services.userservice.services.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@PreAuthorize("isAuthenticated()")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService, UserRepo userRepo) {
        this.addressService = addressService;
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAddresses(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(Authentication authentication, @PathVariable Long addressId) {
        Long userId = getUserId(authentication);
        AddressDTO dto = addressService.getAddressById(userId, addressId);
        if (dto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(Authentication authentication, @RequestBody AddressDTO dto) {
        Long userId = getUserId(authentication);
        if (dto.getStreetAddress() == null || dto.getCity() == null || dto.getState() == null
                || dto.getZipCode() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        AddressDTO created = addressService.addAddress(userId, dto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(Authentication authentication, @PathVariable Long addressId,
            @RequestBody AddressDTO dto) {
        Long userId = getUserId(authentication);
        AddressDTO updated = addressService.updateAddress(userId, addressId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(Authentication authentication, @PathVariable Long addressId) {
        Long userId = getUserId(authentication);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(Authentication authentication, @PathVariable Long addressId) {
        Long userId = getUserId(authentication);
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}