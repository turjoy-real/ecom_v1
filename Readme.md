# E-Commerce Microservices Platform

A comprehensive e-commerce platform built with Spring Boot microservices architecture, featuring OAuth2 authentication, product management, cart functionality, order processing, and payment integration.

## 🏗️ Architecture Overview

### Microservices

- **OAuth Server** - Authentication and authorization using OAuth2 with PKCE
- **User Service** - User management, address management, and profile operations
- **Product Service** - Product catalog, search, and inventory management
- **Cart Service** - Shopping cart operations and item management
- **Order Service** - Order processing, tracking, and management
- **Payment Service** - Payment processing with Razorpay integration
- **Notification Service** - Email and SMS notifications
- **Gateway** - API gateway for routing and security
- **Service Discovery** - Service registration and discovery

### Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0
- **Caching**: Redis
- **Message Broker**: Apache Kafka
- **Search**: Elasticsearch (planned)
- **API Gateway**: Kong
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Payment**: Razorpay

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- MySQL 8.0
- Redis
- Apache Kafka

### Local Development Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd ecom_v1
   ```

2. **Start infrastructure services**

   ```bash
   # Start MySQL, Redis, and Kafka using Docker Compose
   docker-compose up -d
   ```

3. **Configure environment variables**

   ```bash
   # Copy and configure environment files for each service
   cp userservice/src/main/resources/application.yml userservice/src/main/resources/application-local.yml
   # Repeat for other services
   ```

4. **Build and run services**

   ```bash
   # Build all services
   mvn clean install -DskipTests

   # Run services (in separate terminals)
   cd userservice && mvn spring-boot:run
   cd productservice && mvn spring-boot:run
   cd cartservice && mvn spring-boot:run
   cd orderservice && mvn spring-boot:run
   cd paymentservice && mvn spring-boot:run
   cd oauthserver && mvn spring-boot:run
   cd gateway && mvn spring-boot:run
   ```

## 📚 API Documentation

### Authentication

- **OAuth2 Endpoints**: `/oauth2/authorize`, `/oauth2/token`
- **User Management**: `/api/users/**`
- **Documentation**: See [Authentication & Authorization Guide](docs/authentication_authorization.md)

### Product Catalog

- **Products**: `/api/products/**`
- **Categories**: `/api/categories/**`
- **Search**: `/api/products/search/**`
- **Documentation**: See [Product Catalog Guide](docs/product_catalogue.md)

### Cart & Orders

- **Cart**: `/api/cart/**`
- **Orders**: `/api/orders/**`
- **Documentation**: See [Cart Module](docs/cart_module.md) and [Order Management](docs/order_management.md)

### Payments

- **Payment Links**: `/api/payment/create-link`
- **Webhooks**: `/api/payment/webhook`
- **Documentation**: See [Payment Module](docs/payment_module.md)

## 🧪 Testing

### API Testing

- **Comprehensive Testing Guide**: [API Testing Guide](docs/api_testing_guide.md)
- **Order API Testing**: [Order API Testing](docs/order_api_testing.md)
- **General API Testing**: [API Testing](docs/api_testing.md)

### Running Tests

```bash
# Run all tests
mvn test

# Run specific service tests
cd userservice && mvn test
```

## 🚀 Deployment

### Kubernetes Deployment

```bash
# Apply namespace
kubectl apply -f k8s-deployments/namespace.yaml

# Deploy services
kubectl apply -f k8s-deployments/userservice/
kubectl apply -f k8s-deployments/productservice/
kubectl apply -f k8s-deployments/orderservice/
kubectl apply -f k8s-deployments/cartservice/
kubectl apply -f k8s-deployments/paymentservice/

# Deploy infrastructure
kubectl apply -f k8s-deployments/mysql/
kubectl apply -f k8s-deployments/elasticsearch/
kubectl apply -f k8s-deployments/kong/
```

### AWS Deployment

For AWS deployment strategy, see [AWS Deployment Guide](docs/aws_deployment.md)

## 📖 Documentation

### Technical Documentation

- [Technical Implementation Details](docs/technical_implementation_details.md)
- [Module Implementation Details](docs/module_implementation_details.md)
- [Deep Dive Technical Details](docs/deep_dive_technical_details.md)

### Feature Documentation

- [Feature Development Report](docs/feature_development_report.md)
- [Authentication & Authorization](docs/authentication_authorization.md)
- [Product Catalog](docs/product_catalogue.md)
- [Cart Module](docs/cart_module.md)
- [Order Management](docs/order_management.md)
- [Payment Module](docs/payment_module.md)

## 🔧 Development

### Project Structure

```
ecom_v1/
├── userservice/          # User management service
├── productservice/       # Product catalog service
├── cartservice/         # Shopping cart service
├── orderservice/        # Order management service
├── paymentservice/      # Payment processing service
├── oauthserver/         # OAuth2 authorization server
├── gateway/             # API gateway
├── notificationservice/ # Notification service
├── servicediscovery/    # Service discovery
├── k8s-deployments/     # Kubernetes manifests
├── docs/               # Documentation
└── kong.yml           # Kong API gateway configuration
```

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Support

For support and questions:

- Check the [documentation](docs/)
- Review [API testing guides](docs/api_testing_guide.md)
- Open an issue for bugs or feature requests
