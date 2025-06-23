package com.services.common.dtos;

import java.util.List;
import lombok.Data;

@Data
public class UserDTO {
    private final String email;
    private final String name;
    private final boolean emailVerified;
    private final List<String> roles;
}
