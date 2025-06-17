package com.services.orderservice.dtos;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private String reason;
    private String description;
}
