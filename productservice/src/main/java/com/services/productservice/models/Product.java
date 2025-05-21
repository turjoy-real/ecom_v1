package com.services.productservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private Double price;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Category category;
    private String brand;
    private Integer stockQuantity;
    private String imageUrl;
}