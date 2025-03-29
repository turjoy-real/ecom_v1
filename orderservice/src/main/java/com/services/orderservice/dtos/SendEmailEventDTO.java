package com.services.orderservice.dtos;

import lombok.Data;

@Data
public class SendEmailEventDTO {
    private String to;
    private String subject;
    private String body;
    private String from;
}
