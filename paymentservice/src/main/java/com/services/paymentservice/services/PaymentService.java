package com.services.paymentservice.services;

import com.services.paymentservice.dtos.CreatePaymentLinkRequestDto;

public interface PaymentService {

    public String createPaymentLink(CreatePaymentLinkRequestDto paymentRequest);

}
