package com.services.orderservice.clients;

import com.services.common.dtos.AddressDTO;
import com.services.common.dtos.UserDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "userservice")
public interface UserClient {

    @GetMapping("/api/address/{id}")
    AddressDTO getAddressById(
            Authentication authentication, @PathVariable Long addressId);

    @GetMapping("/api/profile")
    UserDTO getMyProfile(@RequestHeader("Authorization") String token);
}
