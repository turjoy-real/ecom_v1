package com.services.common.dtos;

import java.util.List;
import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String name;
    private boolean emailVerified;
    private List<String> roles;
}
