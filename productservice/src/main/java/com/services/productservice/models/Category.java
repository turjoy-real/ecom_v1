package com.services.productservice.models;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Category extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
}
