# E-Commerce Microservices Platform

A comprehensive e-commerce platform built with Spring Boot microservices architecture.

## 🏁 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0
- Redis
- Apache Kafka
- Elasticsearch
- MongoDB (local or Atlas)

---

## 🚀 Deployment Options

### Option 1: Local Development Setup

Follow the instructions below for local development with individual service startup.

### Option 2: Kubernetes Deployment (Recommended for Production)

For production-ready deployment using Docker and Kubernetes with minikube, see our comprehensive guide:

**[📖 Kubernetes Deployment Guide](docs/kubernetes-deployment-guide.md)**

This guide includes:
- Complete Docker and Kubernetes setup
- Automated deployment with minikube
- API Gateway configuration with Kong
- Monitoring with Grafana and Prometheus
- Security best practices with Kubernetes Secrets
- Troubleshooting and scaling guidelines

---

## Installing Prerequisites Locally

### Install Elasticsearch

- Download and extract from [Elasticsearch Downloads](https://www.elastic.co/downloads/elasticsearch)
- Start Elasticsearch:
  ```bash
  ./bin/elasticsearch
  ```
  (or use the Windows `.bat` file)

### Install Kafka

- Download and extract from [Kafka Downloads](https://kafka.apache.org/downloads)
- Start Zookeeper (required for Kafka):
  ```bash
  bin/zookeeper-server-start.sh config/zookeeper.properties
  ```
- Start Kafka broker:
  ```bash
  bin/kafka-server-start.sh config/server.properties
  ```

### Install Redis

- On macOS:
  ```bash
  brew install redis
  brew services start redis
  ```
- On Ubuntu:
  ```bash
  sudo apt-get update
  sudo apt-get install redis-server
  sudo systemctl start redis-server
  ```
- On Windows: Use [Redis for Windows](https://github.com/microsoftarchive/redis/releases) or WSL.

---

## 🚀 Quick Start: Running the Services

### 1. Clone the repository

```bash
git clone https://github.com/turjoy-real/ecom_v1.git
cd ecom_v1
```

### 2. Generate Private and Public Key Files for OAuth Server

The OAuth server requires a private key for signing tokens and a public key for verification. Generate both and place them in `oauthserver/src/main/resources/`:

```bash
# Generate a 2048-bit RSA private key
openssl genpkey -algorithm RSA -out oauthserver/src/main/resources/private-key.pem -pkeyopt rsa_keygen_bits:2048

# Extract the public key from the private key
openssl rsa -pubout -in oauthserver/src/main/resources/private-key.pem -out oauthserver/src/main/resources/public-key.pem
```

### 3. Configure Environment Variables

You can use the `launch_copy.json` template for setting environment variables for different functions in VSCode or IntelliJ IDEA. This helps avoid passing environment variables manually.

### 4. Ensure Infrastructure Services are Running

**Before starting any microservice**, ensure these services are running:

- **MySQL** (Database)
- **MongoDB** (local or Atlas)
- **Redis** (Caching)
- **Kafka** (Message Broker)
- **Elasticsearch** (Search Engine)

Make sure the `application.properties` files of all services are properly configured or the right environment variables are passed to services before they start.

### 5. Build all services

```bash
mvn clean install -DskipTests
```

### 6. Start Services in the Correct Order

**IMPORTANT**: Start services in this exact order to ensure proper service discovery and dependencies:

1. **Service Discovery** (Eureka)
2. **OAuth Server** (Port 9001)
3. **User Service**
4. **Product Service**
5. **Cart Service**
6. **Order Service**
7. **Payment Service**
8. **Gateway** (API Gateway)

### 7. Verify Service Discovery

- Check the Eureka dashboard at `http://localhost:8761/`
- **CRITICAL**: Ensure that local URLs of services are the same as gateway, otherwise gateway cannot detect using service discovery
- **Example of conflict**: `192.168.29.50:paymentservice:5002` and `turjoys-macbook-air.local:oauthserver:9001` will conflict
- All services should use consistent hostnames/IPs in their configuration

### 8. Recommended: Run Using IDE

- Use your preferred IDE (VSCode or IntelliJ IDEA) to run the services
- This helps avoid passing environment variables manually
- Configure launch configurations using the `launch_copy.json` template

---

## 🧪 Testing and Development Setup

### Setting Up Mocks

- Configure mock services for external dependencies
- Set up test doubles for database, message queues, and external APIs
- Use Mockito or similar frameworks for unit testing

### Unit and Integration Tests

- Run unit tests: `mvn test`
- Run integration tests: `mvn verify`
- Configure test profiles for different environments
- Set up test databases and mock external services

### Database Migrations

For database setup and migration scripts, refer to the detailed documentation in [ProductService README](productservice/README.md).

---

## 🔧 Important Configuration Notes

### OAuth Server Configuration

- The OAuth server runs on port **9001**
- If you need to change the port, make necessary changes in the `spa-client` configuration in the OAuth server's `SecurityConfig`
- Ensure the private and public key files (`.pem`) are properly generated and placed in the resources folder

### Service Discovery Considerations

- All services must register with the same Eureka server
- Hostnames and IPs must be consistent across all services
- Check the Eureka dashboard to verify all services are properly registered

---

## 🗂️ Project Structure

```
ecom_v1/
├── userservice/          # User management service
├── productservice/       # Product catalog service
├── cartservice/          # Shopping cart service
├── orderservice/         # Order management service
├── paymentservice/       # Payment processing service
├── oauthserver/          # OAuth2 authorization server (Port 9001)
├── gateway/              # API gateway
├── notificationservice/  # Notification service
├── servicediscovery/     # Service discovery
├── k8s-deployments/      # Kubernetes manifests
├── docs/                 # Documentation
└── kong.yml              # Kong API gateway configuration
```

---

For more details on configuration or troubleshooting, refer to the documentation in the `docs/` directory.
