# Docker & Kubernetes Deployment Guide for E-Commerce Microservices

This guide provides step-by-step instructions to build Docker images, deploy to Minikube, and interconnect all services in the e-commerce platform.

## Prerequisites

- Docker Desktop installed and running
- Minikube installed and running
- kubectl CLI tool
- Java 17+ and Maven 3.6+
- Docker Hub account (or local registry)

## Step 1: Start Minikube and Configure Docker Environment

```bash
# Start Minikube with sufficient resources
minikube start --cpus=4 --memory=8192 --disk-size=20g

# Enable ingress addon for external access
minikube addons enable ingress

# Configure Docker to use Minikube's Docker daemon
eval $(minikube docker-env)

# Verify Minikube is running
kubectl cluster-info
```

## Step 2: Create Docker Images for All Services

### 2.1 Create Dockerfiles for Each Service

First, let's create Dockerfiles for services that don't have them:

**For UserService:**
```dockerfile
# userservice/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/userservice-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For CartService:**
```dockerfile
# cartservice/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/cartservice-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For OrderService:**
```dockerfile
# orderservice/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/orderservice-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For NotificationService:**
```dockerfile
# notificationservice/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/notificationservice-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For OAuthServer:**
```dockerfile
# oauthserver/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/oauthserver-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For Gateway:**
```dockerfile
# gateway/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**For ServiceDiscovery:**
```dockerfile
# servicediscovery/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/servicediscovery-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2.2 Build All Services

```bash
# First build and install the common module to local repository
cd common-modules && mvn clean install -DskipTests && cd ..

# Build all services with Maven
cd userservice && mvn clean package -DskipTests
cd ../productservice && mvn clean package -DskipTests
cd ../cartservice && mvn clean package -DskipTests
cd ../orderservice && mvn clean package -DskipTests
cd ../paymentservice && mvn clean package -DskipTests
cd ../notificationservice && mvn clean package -DskipTests
cd ../oauthserver && mvn clean package -DskipTests
cd ../gateway && mvn clean package -DskipTests
cd ../servicediscovery && mvn clean package -DskipTests
cd ..
```

### 2.3 Build Docker Images

```bash
# Build images for all services
docker build -t userservice:latest userservice/
docker build -t productservice:latest productservice/
docker build -t cartservice:latest cartservice/
docker build -t orderservice:latest orderservice/
docker build -t paymentservice:latest paymentservice/
docker build -t notificationservice:latest notificationservice/
docker build -t oauthserver:latest oauthserver/
docker build -t gateway:latest gateway/
docker build -t servicediscovery:latest servicediscovery/
```

## Step 3: Deploy Infrastructure Services

### 3.1 Create Namespace

```bash
kubectl apply -f k8s-deployments/namespace.yaml
```

### 3.2 Deploy MySQL

```bash
kubectl apply -f k8s-deployments/mysql/
```

### 3.3 Deploy Redis

```yaml
# k8s-deployments/redis/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: ecom_v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
---
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: ecom_v1
spec:
  selector:
    app: redis
  ports:
  - protocol: TCP
    port: 6379
    targetPort: 6379
  type: ClusterIP
```

```bash
kubectl apply -f k8s-deployments/redis/
```

### 3.4 Deploy Kafka

```yaml
# k8s-deployments/kafka/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
  namespace: ecom_v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zookeeper
  template:
    metadata:
      labels:
        app: zookeeper
    spec:
      containers:
      - name: zookeeper
        image: confluentinc/cp-zookeeper:7.4.0
        env:
        - name: ZOOKEEPER_CLIENT_PORT
          value: "2181"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: ecom_v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.4.0
        env:
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "zookeeper:2181"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://kafka:9092"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
        ports:
        - containerPort: 9092
---
apiVersion: v1
kind: Service
metadata:
  name: zookeeper
  namespace: ecom_v1
spec:
  selector:
    app: zookeeper
  ports:
  - protocol: TCP
    port: 2181
    targetPort: 2181
  type: ClusterIP
---
apiVersion: v1
kind: Service
metadata:
  name: kafka
  namespace: ecom_v1
spec:
  selector:
    app: kafka
  ports:
  - protocol: TCP
    port: 9092
    targetPort: 9092
  type: ClusterIP
```

```bash
kubectl apply -f k8s-deployments/kafka/
```

### 3.5 Deploy Elasticsearch

```bash
kubectl apply -f k8s-deployments/elasticsearch/
```

## Step 4: Deploy Microservices

### 4.1 Update Kubernetes Manifests

Update the deployment files to use local images:

```yaml
# Example for userservice/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: userservice
  namespace: ecom_v1
spec:
  replicas: 2
  selector:
    matchLabels:
      app: userservice
  template:
    metadata:
      labels:
        app: userservice
    spec:
      containers:
        - name: userservice
          image: userservice:latest
          imagePullPolicy: Never  # Use local image
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "kubernetes"
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              value: "http://servicediscovery:8761/eureka/"
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:mysql://mysql:3306/userservice"
            - name: SPRING_REDIS_HOST
              value: "redis"
            - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
              value: "kafka:9092"
```

### 4.2 Deploy Services in Order

```bash
# 1. Deploy Service Discovery first
kubectl apply -f k8s-deployments/servicediscovery/

# 2. Deploy OAuth Server
kubectl apply -f k8s-deployments/oauthserver/

# 3. Deploy core services
kubectl apply -f k8s-deployments/userservice/
kubectl apply -f k8s-deployments/productservice/
kubectl apply -f k8s-deployments/cartservice/
kubectl apply -f k8s-deployments/orderservice/
kubectl apply -f k8s-deployments/paymentservice/
kubectl apply -f k8s-deployments/notificationservice/

# 4. Deploy Gateway last
kubectl apply -f k8s-deployments/gateway/
```

## Step 5: Configure Service Interconnection

### 5.1 Create ConfigMaps for Service URLs

```yaml
# k8s-deployments/configmaps.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: service-urls
  namespace: ecom_v1
data:
  USER_SERVICE_URL: "http://userservice:8081"
  PRODUCT_SERVICE_URL: "http://productservice:8082"
  CART_SERVICE_URL: "http://cartservice:8083"
  ORDER_SERVICE_URL: "http://orderservice:8084"
  PAYMENT_SERVICE_URL: "http://paymentservice:8085"
  NOTIFICATION_SERVICE_URL: "http://notificationservice:8086"
  OAUTH_SERVER_URL: "http://oauthserver:8087"
  EUREKA_SERVER_URL: "http://servicediscovery:8761"
```

### 5.2 Update Gateway Configuration

```yaml
# k8s-deployments/gateway/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway
  namespace: ecom_v1
spec:
  replicas: 2
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
        - name: gateway
          image: gateway:latest
          imagePullPolicy: Never
          ports:
            - containerPort: 8080
          env:
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              value: "http://servicediscovery:8761/eureka/"
            - name: SPRING_PROFILES_ACTIVE
              value: "kubernetes"
---
apiVersion: v1
kind: Service
metadata:
  name: gateway
  namespace: ecom_v1
spec:
  selector:
    app: gateway
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: LoadBalancer
```

## Step 6: Expose Gateway API

### 6.1 Using LoadBalancer Service

```bash
# Apply the gateway service
kubectl apply -f k8s-deployments/gateway/service.yaml

# Get the external IP
kubectl get svc -n ecom_v1 gateway
```

### 6.2 Using Ingress Controller

```yaml
# k8s-deployments/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ecom-ingress
  namespace: ecom_v1
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.ecom.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway
            port:
              number: 8080
```

```bash
# Apply ingress
kubectl apply -f k8s-deployments/ingress.yaml

# Add host entry
echo "$(minikube ip) api.ecom.local" | sudo tee -a /etc/hosts
```

### 6.3 Using Kong API Gateway

```bash
# Deploy Kong
kubectl apply -f k8s-deployments/kong/

# Apply Kong configuration
kubectl apply -f kong.yml
```

## Step 7: Verify Deployment

### 7.1 Check Pod Status

```bash
# Check all pods are running
kubectl get pods -n ecom_v1

# Check services
kubectl get svc -n ecom_v1

# Check ingress
kubectl get ingress -n ecom_v1
```

### 7.2 Test Service Connectivity

```bash
# Test gateway access
curl -X GET http://$(minikube ip):8080/actuator/health

# Test individual services
curl -X GET http://$(minikube ip):8081/actuator/health  # User service
curl -X GET http://$(minikube ip):8082/actuator/health  # Product service
```

### 7.3 Monitor Logs

```bash
# Check gateway logs
kubectl logs -f deployment/gateway -n ecom_v1

# Check service discovery logs
kubectl logs -f deployment/servicediscovery -n ecom_v1
```

## Step 8: Access the Application

### 8.1 Get Minikube IP

```bash
minikube ip
```

### 8.2 Access Gateway API

```bash
# Using LoadBalancer
curl -X GET http://$(minikube ip):8080/api/v1/products

# Using Ingress
curl -X GET http://api.ecom.local/api/v1/products

# Using Kong
curl -X GET http://$(minikube ip):8000/api/v1/products
```

## Step 9: Troubleshooting

### 9.1 Common Issues

1. **Pods not starting**: Check resource limits and image availability
2. **Service discovery issues**: Ensure Eureka server is running first
3. **Database connection issues**: Verify MySQL deployment and credentials
4. **Network connectivity**: Check service names and ports

### 9.2 Useful Commands

```bash
# Describe pod for detailed info
kubectl describe pod <pod-name> -n ecom_v1

# Port forward for direct access
kubectl port-forward svc/gateway 8080:8080 -n ecom_v1

# Access Minikube dashboard
minikube dashboard

# View logs for all pods
kubectl logs -l app=gateway -n ecom_v1
```

## Step 10: Cleanup

```bash
# Delete all resources
kubectl delete namespace ecom_v1

# Stop Minikube
minikube stop

# Delete Minikube cluster
minikube delete
```

## Summary

This guide provides a complete workflow for:
1. Building Docker images for all microservices
2. Deploying to Minikube with proper service interconnection
3. Exposing the gateway API through multiple methods
4. Verifying and troubleshooting the deployment

The services are interconnected through Kubernetes service discovery, and the gateway provides a unified API endpoint for all microservices. 