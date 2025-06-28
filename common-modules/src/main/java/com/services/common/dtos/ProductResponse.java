package com.services.common.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String brand;
    private Integer stockQuantity;
    private String imageUrl;
}
