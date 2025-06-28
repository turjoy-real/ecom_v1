package com.services.userservice.dtos;

import java.util.List;

import com.services.common.dtos.UserDTO;
import com.services.userservice.models.Role;
import com.services.userservice.models.User;

public class UserMapper {
    public static UserDTO fromEntity(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setEmailVerified(user.isEmailVerified());
        userDTO.setRoles(roleNames);
        return userDTO;
    }
}
