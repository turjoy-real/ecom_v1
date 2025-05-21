package com.services.productservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDocument {

    @Id
    private String id;

    private String name;
    private String description;
    private Double price;
    private CategoryDocument category;
    private String brand;
    private Integer stockQuantity;
    private String imageUrl;
}
