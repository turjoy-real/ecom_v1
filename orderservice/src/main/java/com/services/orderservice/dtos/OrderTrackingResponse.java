package com.services.orderservice.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderTrackingResponse {
    private String trackingNumber;
    private String carrier;
    private String trackingUrl;
    private String currentStatus;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime lastUpdated;
}