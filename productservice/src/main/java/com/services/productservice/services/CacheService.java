package com.services.productservice.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.services.productservice.dtos.ProductResponse;
import com.services.productservice.models.Product;

@Service
public class CacheService {
    private String productKey = "PRODUCTS";

    public boolean isProductCached(RedisTemplate<Long, Object> redisTemplate, Long id) {
        return redisTemplate.opsForHash().hasKey(id, productKey);
    }

    public void updateProductInCache(RedisTemplate<Long, Object> redisTemplate, Long id, Product product) {
        redisTemplate.opsForHash().put(id, productKey, product);
    }

    public void deleteProductFromCache(RedisTemplate<Long, Object> redisTemplate, Long id) {
        redisTemplate.opsForHash().delete(id, productKey);
    }

    public ProductResponse getProductFromCache(RedisTemplate<Long, Object> redisTemplate, Long id) {
        return (ProductResponse) redisTemplate.opsForHash().get(id, productKey);
    }

    @Async
    public void cacheProduct(RedisTemplate<Long, Object> redisTemplate, Long id, ProductResponse product) {
        redisTemplate.opsForHash().put(id, productKey, product);
    }
}
