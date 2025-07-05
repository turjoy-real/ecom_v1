# Kubernetes Deployment Guide

This guide provides step-by-step instructions for deploying the e-commerce microservices using Docker and Kubernetes with minikube.

## Prerequisites

- Docker Desktop installed and running
- minikube installed
- kubectl CLI tool installed
- Java 17+ and Maven for building services

## Architecture Overview

The e-commerce platform consists of the following microservices:

- **Gateway Service** (Port 9000) - API Gateway using Spring Cloud Gateway
- **User Service** (Port 9002) - User management and authentication
- **OAuth Server** (Port 9001) - OAuth2 authorization server
- **Product Service** (Port 8000) - Product catalog management
- **Cart Service** (Port 7001) - Shopping cart management
- **Order Service** (Port 6000) - Order processing
- **Payment Service** (Port 5002) - Payment processing (Razorpay)
- **Notification Service** (Port 5001) - Email notifications
- **Service Discovery** (Port 8761) - Eureka service registry

### Infrastructure Services

- **MySQL** - Primary database
- **Redis** - Caching and session storage
- **Kafka** - Message queuing
- **Elasticsearch** - Search and analytics
- **Kong** - API Gateway

## Step 1: Start minikube

```bash
# Start minikube with Docker driver
minikube start

# Enable ingress addon
minikube addons enable ingress

# Start tunnel for LoadBalancer services
minikube tunnel
```

## Step 2: Build Docker Images

Build all service images using minikube's Docker daemon:

```bash
# Set Docker environment to use minikube's daemon
eval $(minikube docker-env)

# Build all service images
docker build -t userservice:latest ./userservice
docker build -t productservice:latest ./productservice
docker build -t paymentservice:latest ./paymentservice
docker build -t orderservice:latest ./orderservice
docker build -t cartservice:latest ./cartservice
docker build -t oauthserver:latest ./oauthserver
docker build -t notificationservice:latest ./notificationservice
docker build -t gateway:latest ./gateway
```

## Step 3: Deploy Infrastructure

### Create Namespace

```bash
kubectl apply -f k8s-deployments/namespace.yaml
```

### Deploy Storage

```bash
kubectl apply -f k8s-deployments/storage/
```

### Deploy Database Services

```bash
# Deploy MySQL
kubectl apply -f k8s-deployments/mysql/

# Deploy Redis
kubectl apply -f k8s-deployments/redis/

# Deploy Kafka
kubectl apply -f k8s-deployments/kafka/

# Deploy Elasticsearch
kubectl apply -f k8s-deployments/elasticsearch/
```

### Deploy Configuration

```bash
# Apply secrets (contains sensitive data)
kubectl apply -f k8s-deployments/ecom-secrets.yaml

# Apply configmaps (contains non-sensitive configuration)
kubectl apply -f k8s-deployments/configmaps.yaml
```

## Step 4: Deploy Microservices

Deploy all microservices in the correct order:

```bash
# Deploy service discovery first
kubectl apply -f k8s-deployments/servicediscovery/

# Deploy OAuth server
kubectl apply -f k8s-deployments/oauthserver/

# Deploy core services
kubectl apply -f k8s-deployments/userservice/
kubectl apply -f k8s-deployments/productservice/
kubectl apply -f k8s-deployments/cartservice/
kubectl apply -f k8s-deployments/orderservice/
kubectl apply -f k8s-deployments/paymentservice/
kubectl apply -f k8s-deployments/notificationservice/

# Deploy gateway
kubectl apply -f k8s-deployments/gateway/

# Deploy Kong API Gateway
kubectl apply -f k8s-deployments/kong/

# Deploy monitoring
kubectl apply -f k8s-deployments/monitoring/
```

## Step 5: Verify Deployment

### Check Pod Status

```bash
# Check all pods in the namespace
kubectl get pods -n ecom-v1

# Check services
kubectl get services -n ecom-v1

# Check deployments
kubectl get deployments -n ecom-v1
```

### Check Kong Service

```bash
# Get Kong service details
kubectl get service kong -n ecom-v1
```

The Kong API Gateway will be available at `http://127.0.0.1:8000` when minikube tunnel is running.

## Step 6: Access the APIs

### API Endpoints

All APIs are accessible through Kong Gateway at `http://127.0.0.1:8000`:

- **User Service**: `http://127.0.0.1:8000/api/userdata/`
- **Product Service**: `http://127.0.0.1:8000/api/products/`
- **Cart Service**: `http://127.0.0.1:8000/api/cart/`
- **Order Service**: `http://127.0.0.1:8000/api/orders/`
- **Payment Service**: `http://127.0.0.1:8000/api/payment/`
- **OAuth**: `http://127.0.0.1:8000/oauth/`

### Test API

```bash
# Test product service
curl -X GET http://127.0.0.1:8000/api/products

# Test user service
curl -X GET http://127.0.0.1:8000/api/userdata
```

## Configuration Details

### Environment Variables

The deployment uses centralized configuration:

- **ConfigMap**: `ecom-config` - Contains non-sensitive configuration
- **Secret**: `ecom-secrets` - Contains sensitive data (passwords, API keys)

### Database Configuration

- **MySQL**: `mysql://db-mysql:3306/ecom_db`
- **Redis**: `redis://redis:6379`
- **Elasticsearch**: `http://elasticsearch:9200`

### Service Discovery

- **Eureka Server**: `http://servicediscovery:8761/eureka/`

## Troubleshooting

### Common Issues

1. **Image Pull Errors**
   ```bash
   # Rebuild images with minikube Docker daemon
   eval $(minikube docker-env)
   docker build -t <service>:latest ./<service>
   ```

2. **Pod CrashLoopBackOff**
   ```bash
   # Check pod logs
   kubectl logs <pod-name> -n ecom-v1
   
   # Check pod events
   kubectl describe pod <pod-name> -n ecom-v1
   ```

3. **Service Connection Issues**
   ```bash
   # Check service endpoints
   kubectl get endpoints -n ecom-v1
   
   # Check service logs
   kubectl logs -l app=<service-name> -n ecom-v1
   ```

### Useful Commands

```bash
# Port forward to access services directly
kubectl port-forward service/<service-name> <local-port>:<service-port> -n ecom-v1

# Access minikube dashboard
minikube dashboard

# Check minikube status
minikube status

# Delete and restart minikube
minikube delete
minikube start
```

## Monitoring

### Grafana Dashboard

Access Grafana at the URL provided by:
```bash
kubectl get service grafana -n ecom-v1
```

Default credentials:
- Username: `admin`
- Password: `admin`

### Prometheus Metrics

Prometheus configuration is available in `k8s-deployments/monitoring/prometheus-config.yaml`

## Security

### Secrets Management

All sensitive data is stored in Kubernetes secrets:
- Database passwords
- API keys (Razorpay, Google OAuth)
- SMTP credentials
- Elasticsearch credentials

### Network Policies

Consider implementing network policies to restrict inter-service communication based on your security requirements.

## Scaling

### Horizontal Pod Autoscaling

To enable HPA for services:

```bash
kubectl autoscale deployment <service-name> --cpu-percent=70 --min=2 --max=10 -n ecom-v1
```

### Resource Limits

All deployments include resource requests and limits for CPU and memory. Adjust these based on your requirements.

## Cleanup

To clean up the entire deployment:

```bash
# Delete all resources in the namespace
kubectl delete namespace ecom-v1

# Stop minikube tunnel
# (Ctrl+C in the tunnel terminal)

# Stop minikube
minikube stop
```

## Next Steps

1. **Production Deployment**: Consider using a production-grade Kubernetes cluster (EKS, GKE, AKS)
2. **CI/CD Pipeline**: Set up automated deployment pipelines
3. **Monitoring**: Implement comprehensive monitoring and alerting
4. **Security**: Add network policies and RBAC
5. **Backup**: Implement database backup strategies
6. **SSL/TLS**: Configure SSL certificates for production

## References

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [Spring Cloud Kubernetes](https://spring.io/projects/spring-cloud-kubernetes)
- [Kong Gateway Documentation](https://docs.konghq.com/) 