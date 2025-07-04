package com.services.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.common.dtos.ProductResponse;
import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.services.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.services.productservice.controllers.ProductController.class)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_returnsProductResponse() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Minimal Product")
                .description("Minimal test")
                .price(10.0)
                .category("Minimal")
                .brand("BrandX")
                .stockQuantity(1)
                .imageUrl("http://img.com/x.jpg")
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .brand(request.getBrand())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        Mockito.when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Minimal Product"))
                .andExpect(jsonPath("$.category").value("Minimal"));
    }

    @Test
    void getProductById_returnsProductResponse() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Minimal Product")
                .description("Minimal test")
                .price(10.0)
                .category("Minimal")
                .brand("BrandX")
                .stockQuantity(1)
                .imageUrl("http://img.com/x.jpg")
                .build();
        Mockito.when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Minimal Product"));
    }

    @Test
    void updateProduct_returnsNoContent() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated test")
                .price(20.0)
                .category("Updated")
                .brand("BrandY")
                .stockQuantity(2)
                .imageUrl("http://img.com/y.jpg")
                .build();
        Mockito.doNothing().when(productService).updateProduct(eq(1L), any(ProductRequest.class));

        mockMvc.perform(patch("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_returnsNoContent() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(1L);
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
} 