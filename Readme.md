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

### Comprehensive Testing Strategy

For detailed testing setup and best practices, see our comprehensive testing guide:

**[📖 Product Service Testing Guide](productservice/testing_guide.md)**

This guide covers:
- **Test Pyramid Implementation** - Unit, Integration, and Context Load tests
- **Mocking Strategies** - Proper isolation of dependencies
- **Spring Boot Test Configuration** - Special configurations for microservices testing
- **Troubleshooting Tips** - Common issues and solutions
- **Test Execution** - How to run specific test types

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

For database setup and migration scripts, refer to the detailed documentation:

**[📖 Product Service Database Migration Guide](productservice/db_migration_guide.md)**

This guide covers:
- **Database Setup** - MySQL configuration and prerequisites
- **Migration Scripts** - Flyway migration management
- **Schema Design** - Table structures and relationships
- **Environment Configuration** - Development, testing, and production setups
- **Best Practices** - Migration strategies and performance optimization
- **Troubleshooting** - Common issues and recovery procedures

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

## 📊 Monitoring and Observability

### Spring Boot Actuators

All microservices include Spring Boot Actuator endpoints for monitoring and health checks. These endpoints provide the foundation for comprehensive observability and integrate seamlessly with Prometheus and Grafana.

#### Available Actuator Endpoints

Each service exposes the following monitoring endpoints:

```bash
# Health Checks
GET /actuator/health                    # Overall application health
GET /actuator/health/db                # Database health status
GET /actuator/health/redis             # Redis connection health
GET /actuator/health/elasticsearch     # Elasticsearch health (Product Service)

# Metrics and Monitoring
GET /actuator/metrics                   # All available metrics
GET /actuator/metrics/{metric.name}    # Specific metric details
GET /actuator/prometheus               # Prometheus-compatible metrics

# Application Information
GET /actuator/info                     # Application information
GET /actuator/env                      # Environment variables
GET /actuator/configprops              # Configuration properties
GET /actuator/beans                    # Spring beans information

# Application Management
GET /actuator/mappings                 # Request mappings
GET /actuator/loggers                  # Logger configurations
POST /actuator/loggers/{logger.name}  # Update logger level
```

#### Key Metrics Exposed

- **JVM Metrics**: Memory usage, garbage collection, thread statistics
- **HTTP Metrics**: Request counts, response times, error rates
- **Database Metrics**: Connection pool status, query performance
- **Custom Business Metrics**: Order counts, payment success rates, user registrations

### Prometheus Integration

The actuator endpoints are designed to work seamlessly with Prometheus for metrics collection:

#### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ecommerce-microservices'
    static_configs:
      - targets: 
        - 'localhost:8080'  # Gateway
        - 'localhost:9001'  # OAuth Server
        - 'localhost:5001'  # User Service
        - 'localhost:5002'  # Product Service
        - 'localhost:5003'  # Cart Service
        - 'localhost:5004'  # Order Service
        - 'localhost:5005'  # Payment Service
        - 'localhost:5006'  # Notification Service
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

#### Metrics Collection

Prometheus automatically scrapes the `/actuator/prometheus` endpoint from each service, collecting:
- **System Metrics**: CPU, memory, disk usage
- **Application Metrics**: Request rates, response times, error rates
- **Business Metrics**: Custom counters and gauges
- **Dependency Health**: Database, Redis, Elasticsearch status

### Grafana Dashboards

The collected metrics can be visualized using Grafana dashboards:

#### Pre-configured Dashboards

1. **System Overview Dashboard**
   - Service health status
   - Response time trends
   - Error rate monitoring
   - Resource utilization

2. **Business Metrics Dashboard**
   - Order processing rates
   - Payment success/failure rates
   - User registration trends
   - Product catalog statistics

3. **Infrastructure Dashboard**
   - Database performance
   - Redis cache hit rates
   - Kafka message throughput
   - Elasticsearch query performance

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "E-Commerce Microservices",
    "panels": [
      {
        "title": "Service Health",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"ecommerce-microservices\"}",
            "legendFormat": "{{instance}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_sum[5m])",
            "legendFormat": "{{instance}} - {{uri}}"
          }
        ]
      }
    ]
  }
}
```

### Alerting Configuration

Set up alerts for critical metrics:

```yaml
# alerting.yml
groups:
  - name: ecommerce-alerts
    rules:
      - alert: ServiceDown
        expr: up{job="ecommerce-microservices"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.instance }} is down"
          
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate on {{ $labels.instance }}"
          
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time on {{ $labels.instance }}"
```

### Kubernetes Monitoring

When deployed on Kubernetes, the monitoring stack includes:

#### Prometheus Operator
- **ServiceMonitor**: Automatically discovers services
- **PrometheusRule**: Defines alerting rules
- **AlertManager**: Handles alert routing and notifications

#### Grafana Operator
- **Grafana**: Pre-configured dashboards
- **DataSource**: Automatic Prometheus connection
- **Dashboard**: Import business-specific dashboards

### Monitoring Best Practices

1. **Health Checks**: All services implement comprehensive health checks
2. **Metrics Collection**: Standardized metrics across all services
3. **Alerting**: Proactive alerting for critical issues
4. **Dashboard**: Real-time visibility into system performance
5. **Logging**: Structured logging for correlation with metrics

### Troubleshooting Monitoring

#### Common Issues

1. **Actuator Endpoints Not Accessible**
   ```bash
   # Check if actuator is enabled
   curl http://localhost:8080/actuator/health
   
   # Verify security configuration
   # Ensure /actuator/** endpoints are accessible
   ```

2. **Prometheus Not Scraping**
   ```bash
   # Check Prometheus targets
   curl http://localhost:9090/api/v1/targets
   
   # Verify service endpoints
   curl http://localhost:8080/actuator/prometheus
   ```

3. **Grafana Dashboard Issues**
   ```bash
   # Check data source connection
   # Verify Prometheus URL in Grafana
   # Check dashboard queries
   ```

#### Monitoring Commands

```bash
# Check service health
curl -s http://localhost:8080/actuator/health | jq

# View metrics
curl -s http://localhost:8080/actuator/metrics | jq

# Check Prometheus targets
curl -s http://localhost:9090/api/v1/targets | jq

# Test Grafana connection
curl -s http://localhost:3000/api/health
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
