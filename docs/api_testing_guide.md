# E-commerce API Testing Guide

## 1. Authentication & User Management

### User Registration

```http
POST /api/auth/register
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "securePassword123",
    "name": "John Doe",
    "phone": "+1234567890"
}
```

Expected Response: 201 Created with user details and verification token

### User Login

```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "securePassword123"
}
```

Expected Response: 200 OK with JWT token

### Password Reset Request

```http
POST /api/auth/password-reset-request
Content-Type: application/json

{
    "email": "user@example.com"
}
```

Expected Response: 200 OK with reset token

### Reset Password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
    "token": "reset-token",
    "newPassword": "newSecurePassword123"
}
```

Expected Response: 200 OK

## 2. Profile Management

### Get User Profile

```http
GET /api/user/profile
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with profile details

```json
{
  "id": "123",
  "email": "user@example.com",
  "name": "John Doe",
  "emailVerified": true,
  "createdAt": "2024-03-20T10:00:00",
  "updatedAt": "2024-03-20T10:00:00"
}
```

### Update User Profile

```http
PUT /api/user/profile
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "name": "John Smith"
}
```

Expected Response: 200 OK with updated profile

```json
{
  "id": "123",
  "email": "user@example.com",
  "name": "John Smith",
  "emailVerified": true,
  "createdAt": "2024-03-20T10:00:00",
  "updatedAt": "2024-03-20T10:30:00"
}
```

## 3. Role Management

### Create Role

```http
POST /api/roles
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "name": "ADMIN"
}
```

Expected Response: 201 Created with role details

### Get All Roles

```http
GET /api/roles
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with list of roles

### Get Role by Name

```http
GET /api/roles/{name}
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with role details

### Delete Role

```http
DELETE /api/roles/{name}
Authorization: Bearer {jwt_token}
```

Expected Response: 204 No Content

### Add Role to User

```http
POST /api/roles/user/add
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "userEmail": "user@example.com",
    "roleName": "ADMIN"
}
```

Expected Response: 200 OK

### Remove Role from User

```http
POST /api/roles/user/remove
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "userEmail": "user@example.com",
    "roleName": "ADMIN"
}
```

Expected Response: 200 OK

### Get User Roles

```http
GET /api/roles/user/{email}
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with list of user's roles

## 4. Address Management

### Add New Address

```http
POST /api/users/addresses
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "isDefault": true
}
```

Expected Response: 201 Created with address details

### Get User Addresses

```http
GET /api/users/addresses
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with list of addresses

### Update Address

```http
PUT /api/users/addresses/{addressId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "street": "456 Park Ave",
    "city": "New York",
    "state": "NY",
    "zipCode": "10022",
    "country": "USA",
    "isDefault": true
}
```

Expected Response: 200 OK with updated address

### Delete Address

```http
DELETE /api/users/addresses/{addressId}
Authorization: Bearer {jwt_token}
```

Expected Response: 204 No Content

## 5. Product Catalog

### Get Products

```http
GET /api/products?page=0&size=10&sort=price,asc
```

Parameters:

- `page`: Page number (0-based)
- `size`: Items per page
- `sort`: Sort field and direction
- `category`: Filter by category
- `minPrice`: Minimum price
- `maxPrice`: Maximum price
- `search`: Search term

Expected Response: 200 OK with paginated products

### Get Product Details

```http
GET /api/products/{productId}
```

Expected Response: 200 OK with product details

### Create Product (Admin)

```http
POST /api/products
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "name": "Product Name",
    "description": "Product Description",
    "price": 99.99,
    "category": "Electronics",
    "stock": 100,
    "images": ["url1", "url2"]
}
```

Expected Response: 201 Created with product details

### Update Product (Admin)

```http
PUT /api/products/{productId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "name": "Updated Name",
    "price": 89.99,
    "stock": 50
}
```

Expected Response: 200 OK with updated product

### Delete Product (Admin)

```http
DELETE /api/products/{productId}
Authorization: Bearer {jwt_token}
```

Expected Response: 204 No Content

## 6. Cart Management

### Get Cart

```http
GET /api/cart
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with cart details

### Add Item to Cart

```http
POST /api/cart/items
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "productId": "prod123",
    "quantity": 2
}
```

Expected Response: 200 OK with updated cart

### Update Cart Item

```http
PUT /api/cart/items/{itemId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "quantity": 3
}
```

Expected Response: 200 OK with updated cart

### Remove Cart Item

```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with updated cart

### Clear Cart

```http
DELETE /api/cart
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK

## 7. Order Management

### Create Order

```http
POST /api/orders
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "shippingAddressId": "addr123",
    "paymentMethod": "CREDIT_CARD"
}
```

Expected Response: 201 Created with order details

### Get Order

```http
GET /api/orders/{orderId}
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with order details

### Get User Orders

```http
GET /api/orders/user/{userId}?status=CREATED&paymentStatus=PENDING
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with list of orders

### Update Order Status

```http
PATCH /api/orders/{orderId}/status?status=PROCESSING
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with updated order

### Cancel Order

```http
POST /api/orders/{orderId}/cancel
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK

### Get Order Tracking

```http
GET /api/orders/{orderId}/tracking
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with tracking details

### Create Return Request

```http
POST /api/orders/{orderId}/return
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "reason": "Wrong size",
    "description": "Need to exchange for larger size"
}
```

Expected Response: 201 Created with return request details

## 8. Payment Processing

### Create Payment Link

```http
POST /api/payments/link
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "orderId": "order123",
    "amount": 99.99,
    "currency": "USD"
}
```

Expected Response: 200 OK with payment link

### Get Payment Status

```http
GET /api/payments/{paymentId}/status
Authorization: Bearer {jwt_token}
```

Expected Response: 200 OK with payment status

### Process Refund

```http
POST /api/payments/{paymentId}/refund
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "amount": 99.99,
    "reason": "Customer return"
}
```

Expected Response: 200 OK with refund details

## Testing Scenarios

### 1. Complete Purchase Flow

1. Register new user
2. Add shipping address
3. Browse products
4. Add items to cart
5. Create order
6. Process payment
7. Track order
8. Request return if needed

### 2. User Management Flow

1. Register
2. Verify email
3. Login
4. Update profile
5. Add/update addresses
6. Request password reset
7. Reset password
8. Login with new password

### 3. Product Management Flow (Admin)

1. Create product
2. Update product details
3. Update stock
4. Delete product
5. Verify product listing

### 4. Cart Management Flow

1. Add items to cart
2. Update quantities
3. Remove items
4. Clear cart
5. Verify cart totals

### 5. Order Management Flow

1. Create order
2. Update status
3. Add tracking
4. Process return
5. Verify analytics

### 6. Payment Flow

1. Create payment link
2. Process payment
3. Verify payment status
4. Process refund if needed

## Error Cases to Test

### Authentication

1. Invalid credentials
2. Expired token
3. Invalid token
4. Missing authorization
5. Unauthorized access

### Products

1. Invalid product ID
2. Out of stock
3. Invalid price
4. Missing required fields
5. Duplicate product

### Cart

1. Invalid quantity
2. Out of stock items
3. Invalid product ID
4. Empty cart operations
5. Cart limits

### Orders

1. Invalid order ID
2. Invalid status transition
3. Cancel delivered order
4. Invalid return request
5. Duplicate order

### Payments

1. Invalid amount
2. Failed payment
3. Invalid currency
4. Duplicate payment
5. Invalid refund

## Expected Response Codes

- 200 OK: Successful operation
- 201 Created: Resource created
- 204 No Content: Successful deletion
- 400 Bad Request: Invalid input
- 401 Unauthorized: Missing/invalid auth
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource already exists
- 422 Unprocessable Entity: Validation error
- 500 Internal Server Error: Server error

## Testing Tools

1. Postman Collection

   - Import the provided collection
   - Set up environment variables
   - Configure authentication
   - Run automated tests

2. cURL Commands

   - Use provided commands
   - Replace placeholders
   - Test individual endpoints

3. Automated Tests
   - Run test suite
   - Check coverage
   - Verify all scenarios

## Environment Setup

1. Development

   - Base URL: http://localhost:8080
   - Database: H2
   - Cache: Redis
   - Message Queue: Kafka

2. Testing

   - Base URL: http://localhost:8081
   - Database: H2
   - Cache: Redis
   - Message Queue: Kafka

3. Production
   - Base URL: https://api.example.com
   - Database: PostgreSQL
   - Cache: Redis
   - Message Queue: Kafka
