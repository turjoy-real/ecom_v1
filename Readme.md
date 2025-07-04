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
git clone <repository-url>
cd ecom_v1
```

### 2. Start infrastructure services and core dependencies
- Ensure the following are running **before starting any microservice**:
  - **Service Discovery** (Eureka):
    ```bash
    cd servicediscovery && mvn spring-boot:run
    ```
  - **OAuth Server** (for authentication):
    ```bash
    cd oauthserver && mvn spring-boot:run
    ```
  - **Elasticsearch**
  - **Redis**
  - **MongoDB** (local or Atlas)
  - **Kafka**
  - **MySQL**

  You can use your preferred method to start these services (local installations, cloud services, or your own container setup).

### 3. Generate private and public key files for OAuth Server
- The OAuth server requires a private key for signing tokens and a public key for verification. Generate both and place them in `oauthserver/src/main/resources/`:

```bash
# Generate a 4096-bit RSA private key
openssl genpkey -algorithm RSA -out oauthserver/src/main/resources/private-key.pem -pkeyopt rsa_keygen_bits:2048

# Extract the public key from the private key
openssl rsa -pubout -in oauthserver/src/main/resources/private-key.pem -out oauthserver/src/main/resources/public-key.pem
```

### 4. Configure application.properties for Each Service
- Each service requires its own `application.properties` file for configuration.
- Example locations:
  - `userservice/src/main/resources/application.properties`
  - `productservice/src/main/resources/application.properties`
  - `cartservice/src/main/resources/application.properties`
  - `orderservice/src/main/resources/application.properties`
  - `paymentservice/src/main/resources/application.properties`
  - `notificationservice/src/main/resources/application.properties`
  - `oauthserver/src/main/resources/application.properties`
- You can use editors like **VSCode** or **IntelliJ IDEA** to easily edit these files. Both provide syntax highlighting and search features for `.properties` files.
- Example configuration for a service:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/your_db
  spring.datasource.username=your_user
  spring.datasource.password=your_pass
  spring.data.redis.url=redis://localhost:6379
  spring.kafka.bootstrap-servers=localhost:9092
  # Add other service-specific properties as needed
  ```

### 5. Build all services
```bash
mvn clean install -DskipTests
```

### 6. Run each service (in separate terminals, after dependencies are up)
```bash
cd userservice && mvn spring-boot:run
cd productservice && mvn spring-boot:run
cd cartservice && mvn spring-boot:run
cd orderservice && mvn spring-boot:run
cd paymentservice && mvn spring-boot:run
cd notificationservice && mvn spring-boot:run
cd gateway && mvn spring-boot:run
```

---

## 🗂️ Project Structure

```
ecom_v1/
├── userservice/          # User management service
├── productservice/       # Product catalog service
├── cartservice/          # Shopping cart service
├── orderservice/         # Order management service
├── paymentservice/       # Payment processing service
├── oauthserver/          # OAuth2 authorization server
├── gateway/              # API gateway
├── notificationservice/  # Notification service
├── servicediscovery/     # Service discovery
├── k8s-deployments/      # Kubernetes manifests
├── docs/                 # Documentation
└── kong.yml              # Kong API gateway configuration
```

---

For more details on configuration or troubleshooting, refer to the documentation in the `docs/` directory.
