package com.services.userservice.dtos;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class UpdateAddressRequest {
    @Size(min = 3, max = 255, message = "Street address must be between 3 and 255 characters")
    private String streetAddress;

    @Size(min = 3, max = 100, message = "City must be between 3 and 100 characters")
    private String city;

    @Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
    private String state;

    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @Size(min = 1, max = 20, message = "Zip code must be between 1 and 20 characters")
    private String zipCode;

    private boolean isDefault;

    @Size(min = 1, max = 50, message = "Label must be between 1 and 50 characters")
    private String label;

    @Size(max = 255, message = "Additional info must be at most 255 characters")
    private String additionalInfo;
}