package com.services.orderservice.clients;

import com.services.common.dtos.UserDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "userservice")
public interface UserClient {

    @GetMapping("/api/profile")
    ResponseEntity<UserDTO> getMyProfile(@RequestHeader("Authorization") String token);

    @GetMapping("/actuator/health")
    ResponseEntity<String> health();
}
