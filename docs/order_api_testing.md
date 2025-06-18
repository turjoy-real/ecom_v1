# Order Management API Testing Guide

## 1. Order Creation and Retrieval

### Create New Order

```http
POST /api/orders
Content-Type: application/json

{
    "userId": "user123",
    "items": [
        {
            "productId": "prod1",
            "quantity": 2
        },
        {
            "productId": "prod2",
            "quantity": 1
        }
    ],
    "shippingAddressId": "addr123"
}
```

Expected Response: 200 OK with order details

### Get Order by ID

```http
GET /api/orders/{orderId}
```

Expected Response: 200 OK with order details

### Get User Orders

```http
GET /api/orders/user/{userId}?status=CREATED&paymentStatus=PENDING&sortBy=orderDate&sortDirection=desc
```

Parameters:

- `status` (optional): Filter by order status
- `paymentStatus` (optional): Filter by payment status
- `sortBy` (optional): Sort field (orderDate, totalAmount, status, paymentStatus)
- `sortDirection` (optional): Sort direction (asc, desc)

Expected Response: 200 OK with list of orders

## 2. Order Status Management

### Update Order Status

```http
PATCH /api/orders/{orderId}/status?status=PROCESSING
```

Status values:

- CREATED
- PROCESSING
- SHIPPED
- DELIVERED
- CANCELLED

Expected Response: 200 OK with updated order

### Cancel Order

```http
POST /api/orders/{orderId}/cancel
```

Expected Response: 200 OK

## 3. Order Tracking

### Get Order Tracking

```http
GET /api/orders/{orderId}/tracking
```

Expected Response: 200 OK with tracking details

### Update Order Tracking

```http
POST /api/orders/{orderId}/tracking?trackingNumber=TRK123&carrier=UPS
```

Expected Response: 200 OK with updated tracking details

## 4. Return Requests

### Create Return Request

```http
POST /api/orders/{orderId}/return
Content-Type: application/json

{
    "reason": "Wrong size",
    "description": "Item received in wrong size, need to exchange for larger size"
}
```

Expected Response: 200 OK with return request details

### Get Return Request

```http
GET /api/returns/{returnId}
```

Expected Response: 200 OK with return request details

### Update Return Status

```http
PATCH /api/returns/{returnId}/status?status=APPROVED
```

Status values:

- PENDING
- APPROVED
- REJECTED
- COMPLETED

Expected Response: 200 OK with updated return request

### List Return Requests

```http
GET /api/returns?status=PENDING
```

Expected Response: 200 OK with list of return requests

## 5. Order Analytics

### Get User Order Analytics

```http
GET /api/orders/user/{userId}/analytics
```

Expected Response: 200 OK with analytics data including:

- Total orders
- Total spent
- Status breakdown
- Payment status breakdown

## Testing Scenarios

### 1. Complete Order Flow

1. Create new order
2. Verify order status is CREATED
3. Update order status to PROCESSING
4. Add tracking information
5. Update order status to SHIPPED
6. Update order status to DELIVERED

### 2. Return Flow

1. Create return request for delivered order
2. Verify return status is PENDING
3. Update return status to APPROVED
4. Verify return label URL is generated
5. Update return status to COMPLETED

### 3. Order Analytics

1. Create multiple orders with different statuses
2. Get user analytics
3. Verify analytics data matches created orders

### 4. Error Cases

1. Try to cancel a delivered order
2. Try to create return for non-delivered order
3. Try to update order with invalid status
4. Try to get tracking for non-existent order
5. Try to create duplicate return request

## Expected Response Codes

- 200 OK: Successful operation
- 201 Created: Resource created successfully
- 400 Bad Request: Invalid input
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 500 Internal Server Error: Server error

## Testing Tools

1. Postman Collection

   - Import the provided Postman collection
   - Set up environment variables for base URL and authentication
   - Run the collection to test all endpoints

2. cURL Commands

   - Use the provided cURL commands for quick testing
   - Replace placeholders with actual values

3. Automated Tests
   - Run the test suite to verify all functionality
   - Check test coverage report
