package com.services.orderservice.events;

import lombok.Data;

@Data
public class PaymentVerifiedEvent {
    private String orderNumber;
    private boolean paymentSuccess;
    private String transactionId;
}
