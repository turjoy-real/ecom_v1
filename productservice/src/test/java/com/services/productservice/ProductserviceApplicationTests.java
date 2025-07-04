package com.services.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("removal")
class ProductserviceApplicationTests {

    @Autowired
	private MockMvc mockMvc;
    
    @MockBean
    private com.services.productservice.repositories.ProductRepository productRepository;

    @MockBean
    private com.services.productservice.repositories.ProductElasticsearchRepository productElasticsearchRepository;

    @MockBean
    private com.services.productservice.repositories.CategoryRepository categoryRepository;

    @MockBean
    private org.springframework.data.redis.core.RedisTemplate<Long, Object> redisTemplate;

    @MockBean
    private com.services.productservice.services.CacheService cacheService;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }

}
