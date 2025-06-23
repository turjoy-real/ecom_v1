package com.services.common.dtos;

import lombok.Data;

@Data
public class PaymentLinkResponseDto {
    private String id;
    private String shortUrl;
    private String status;
    private double amount;
    private double amountPaid;
    private String currency;
    private String description;
    private String referenceId;
    private boolean acceptPartial;
    private double firstMinPartialAmount;
    private boolean reminderEnable;
    private CustomerDto customer;
    private NotifyDto notify;
    private long createdAt;
    private long updatedAt;

    @Data
    public static class CustomerDto {
        private String name;
        private String email;
        private String contact;
    }

    @Data
    public static class NotifyDto {
        private boolean sms;
        private boolean email;
    }
}