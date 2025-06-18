# API Testing Guide

## Implemented APIs

### User Management Module

- **POST /api/auth/register**: Register new user
- **POST /api/auth/login**: User login
- **POST /api/auth/password-reset**: Request password reset
- **POST /api/auth/password-reset/confirm**: Confirm password reset

### Product Catalog Module

- **GET /api/products**: Retrieve all products with pagination.
- **GET /api/products/{id}**: Retrieve a specific product by ID.
- **GET /api/products/{id}/verify-stock**: Verify stock availability for a product.
- **GET /api/products/search/advanced**: Search products by multiple criteria.
- **GET /api/products/search/stock**: Search products by stock availability.
- **GET /api/products/search/full-text**: Perform full-text search on products.
- **GET /api/products/category/{category}**: Retrieve products by category.
- **PATCH /api/products/{id}**: Update a product.
- **DELETE /api/products/{id}**: Delete a product.

### Cart Module

- **POST /api/cart/add**: Add an item to the cart.
- **GET /api/cart**: Retrieve the user's cart.
- **DELETE /api/cart/{itemId}**: Remove an item from the cart.

### Order Management Module

- **POST /api/orders**: Create a new order.
- **GET /api/orders/{id}**: Retrieve a specific order by ID.
- **GET /api/orders/user/{userId}**: Retrieve all orders for a specific user.

### Payment Module

- **POST /api/payment**: Create a payment link.
- **POST /api/payment/webhook**: Handle payment webhooks.

## Not Implemented APIs

### User Management Module

- **GET /api/users/profile**: Get user profile
- **PUT /api/users/profile**: Update user profile

### Cart & Checkout Module

- **POST /api/checkout**: Complete checkout process
- **POST /api/checkout/delivery-address**: Add/update delivery address
- **POST /api/checkout/payment-method**: Select payment method

### Order Management Module

- **GET /api/orders/{id}/tracking**: Get order tracking status
- **POST /api/orders/{id}/cancel**: Cancel an order

### Payment Module

- **GET /api/payment/methods**: Get available payment methods
- **POST /api/payment/receipt**: Generate payment receipt
- **GET /api/payment/history**: Get payment history

## API Flows with Screenshots

### User Registration Flow

1. **API Request**: POST /api/auth/register

   - [Screenshot: API request body and response]
   - Expected Response: 201 Created with user details

2. **Email Verification**

   - [Screenshot: Email received with verification link]
   - [Screenshot: Email content showing verification code]

3. **Email Verification Completion**
   - [Screenshot: Browser window showing verification success]
   - [Screenshot: Redirect to login page]

### User Login Flow

1. **API Request**: POST /api/auth/login

   - [Screenshot: API request with credentials]
   - [Screenshot: API response with JWT token]

2. **Token Usage**
   - [Screenshot: API request with JWT token in header]
   - [Screenshot: Protected resource access]

### Password Reset Flow

1. **Request Reset**

   - [Screenshot: API request for password reset]
   - [Screenshot: Email received with reset link]

2. **Reset Password**
   - [Screenshot: Browser window with reset form]
   - [Screenshot: API request to confirm reset]
   - [Screenshot: Success message]

### Product Search Flow

1. **Search API**

   - [Screenshot: API request with search parameters]
   - [Screenshot: API response with product list]

2. **Product Details**
   - [Screenshot: API request for specific product]
   - [Screenshot: API response with product details]

### Cart Management Flow

1. **Add to Cart**

   - [Screenshot: API request to add item]
   - [Screenshot: Cart response with updated items]

2. **View Cart**
   - [Screenshot: API request for cart contents]
   - [Screenshot: Cart response with items and total]

### Order Creation Flow

1. **Address Management**

   - [Screenshot: API request to add new address]
   - [Screenshot: API response with address details]
   - [Screenshot: List of user's addresses]
   - [Screenshot: Setting default address]

2. **Create Order**

   - [Screenshot: API request with order details and selected address]
   - [Screenshot: Order confirmation response]

3. **Payment Initiation**

   - [Screenshot: API request for payment link]
   - [Screenshot: Payment gateway interface]

4. **Payment Confirmation**
   - [Screenshot: Webhook notification]
   - [Screenshot: Order status update]
   - [Screenshot: Order confirmation email]

### Error Handling Examples

1. **Invalid Credentials**

   - [Screenshot: API error response]
   - [Screenshot: Error message display]

2. **Validation Errors**

   - [Screenshot: API validation error response]
   - [Screenshot: Form validation display]

3. **Authorization Errors**
   - [Screenshot: API unauthorized response]
   - [Screenshot: Access denied display]

## Kafka Message Purpose

When a user adds items to the cart, a Kafka message is sent to notify other services (e.g., inventory, order management) about the change in the cart. This event-driven approach ensures that all related services are updated in real-time, maintaining data consistency and enabling features like stock verification and order processing. Kafka is used because it provides a reliable, scalable, and fault-tolerant messaging system that can handle high volumes of data and ensure that all services are notified of changes in a timely manner. This is crucial for maintaining accurate stock levels and processing orders efficiently.

---

### References

- [Spring Web](https://spring.io/projects/spring-web)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
