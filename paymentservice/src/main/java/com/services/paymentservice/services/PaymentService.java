package com.services.paymentservice.services;

import com.services.common.dtos.CreatePaymentLinkRequestDto;

public interface PaymentService {
    public String createPaymentLink(CreatePaymentLinkRequestDto paymentRequest);

    public String handleWebhook(String payload, String signature, String eventId);
}
