package com.services.common.dtos;

import lombok.Data;

@Data
public class CreatePaymentLinkRequestDto {
    private String orderId;
    private String userId;
    private double amount;
    private String currency;
    private String customerName;
    private String customerEmail;
    private String customerContact;
    private String description;
    private boolean acceptPartial;
    private double firstMinPartialAmount;
    private boolean reminderEnable;
}
