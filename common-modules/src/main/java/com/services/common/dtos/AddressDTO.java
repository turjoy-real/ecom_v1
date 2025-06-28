package com.services.common.dtos;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class AddressDTO {
    private Long id;

    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must be at most 255 characters")
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @NotBlank(message = "Zip code is required")
    @Size(max = 20, message = "Zip code must be at most 20 characters")
    private String zipCode;

    private boolean isDefault;

    @NotBlank(message = "Label is required")
    @Size(max = 50, message = "Label must be at most 50 characters")
    private String label;

    @Size(max = 255, message = "Additional info must be at most 255 characters")
    private String additionalInfo;
}