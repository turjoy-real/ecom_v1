package com.services.userservice.dtos;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault;
    private String label;
    private String additionalInfo;
}