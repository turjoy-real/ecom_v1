package com.services.cartservice.services;

import com.services.cartservice.dtos.CartItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceClient {
    private final RestTemplate restTemplate;
    @Value("${order.service.url:http://orderservice}")
    private String orderServiceUrl;

    public String placeOrder(String userId, String addressId, List<CartItemDTO> cartItems) {
        // Map CartItemDTO to OrderItemRequest
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItemDTO item : cartItems) {
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("productId", item.getProductId());
            orderItem.put("quantity", item.getQuantity());
            orderItems.add(orderItem);
        }
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", userId);
        orderRequest.put("items", orderItems);
        orderRequest.put("shippingAddressId", addressId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderRequest, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(orderServiceUrl + "/api/orders", entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object paymentLink = response.getBody().get("paymentLink");
            if (paymentLink != null) {
                return paymentLink.toString();
            }
            // fallback: return order id or error
            return response.getBody().getOrDefault("id", "Order placed").toString();
        }
        throw new RuntimeException("Failed to place order");
    }
}