package com.services.orderservice.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReturnRequestResponse {
    private Long id;
    private String orderId;
    private String reason;
    private String description;
    private String status;
    private String returnLabelUrl;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
}