# Quick Start Guide - Docker & Kubernetes Deployment

## 🚀 One-Command Deployment

The easiest way to deploy your e-commerce microservices to Minikube is using the automated script:

```bash
./deploy-to-minikube.sh
```

This script will:
1. ✅ Check all prerequisites
2. 🐳 Start Minikube with proper resources
3. 🔨 Build all services with Maven
4. 📦 Create Docker images for all services
5. ☸️ Deploy to Kubernetes in the correct order
6. 🔍 Wait for all pods to be ready
7. 📊 Show deployment status and access information

## 📋 Prerequisites

Before running the deployment, ensure you have:

- **Docker Desktop** installed and running
- **Minikube** installed
- **kubectl** CLI tool
- **Java 17+** and **Maven 3.6+**
- **Git** (to clone the repository)

## 🔧 Manual Deployment Steps

If you prefer to deploy manually, follow these steps:

### 1. Start Minikube
```bash
minikube start --cpus=4 --memory=8192 --disk-size=20g
minikube addons enable ingress
eval $(minikube docker-env)
```

### 2. Build Services
```bash
# First build and install the common module
cd common-modules && mvn clean install -DskipTests && cd ..

# Build all services
for service in userservice productservice cartservice orderservice paymentservice notificationservice oauthserver gateway servicediscovery; do
    cd $service && mvn clean package -DskipTests && cd ..
done
```

### 3. Build Docker Images
```bash
# Build images for all services
for service in userservice productservice cartservice orderservice paymentservice notificationservice oauthserver gateway servicediscovery; do
    docker build -t $service:latest $service/
done
```

### 4. Deploy to Kubernetes
```bash
# Create namespace
kubectl apply -f k8s-deployments/namespace.yaml

# Deploy infrastructure
kubectl apply -f k8s-deployments/mysql/
kubectl apply -f k8s-deployments/redis/
kubectl apply -f k8s-deployments/kafka/
kubectl apply -f k8s-deployments/elasticsearch/

# Deploy microservices in order
kubectl apply -f k8s-deployments/servicediscovery/
kubectl apply -f k8s-deployments/oauthserver/
kubectl apply -f k8s-deployments/userservice/
kubectl apply -f k8s-deployments/productservice/
kubectl apply -f k8s-deployments/cartservice/
kubectl apply -f k8s-deployments/orderservice/
kubectl apply -f k8s-deployments/paymentservice/
kubectl apply -f k8s-deployments/notificationservice/
kubectl apply -f k8s-deployments/gateway/

# Apply configuration
kubectl apply -f k8s-deployments/configmaps.yaml
kubectl apply -f k8s-deployments/ingress.yaml
```

## 🌐 Access Your Application

After deployment, you can access your application through:

### Method 1: LoadBalancer Service
```bash
# Get Minikube IP and Gateway port
MINIKUBE_IP=$(minikube ip)
GATEWAY_PORT=$(kubectl get svc gateway -n ecom_v1 -o jsonpath='{.spec.ports[0].port}')

# Access Gateway API
curl http://$MINIKUBE_IP:$GATEWAY_PORT/api/v1/products
```

### Method 2: Ingress (Recommended)
```bash
# Add host entries
MINIKUBE_IP=$(minikube ip)
echo "$MINIKUBE_IP api.ecom.local ecom.local" | sudo tee -a /etc/hosts

# Access via ingress
curl http://api.ecom.local/api/v1/products
curl http://ecom.local/api/v1/users
```

### Method 3: Port Forwarding
```bash
# Forward gateway port
kubectl port-forward svc/gateway 8080:8080 -n ecom_v1

# Access locally
curl http://localhost:8080/api/v1/products
```

## 📊 Monitoring & Debugging

### Check Pod Status
```bash
kubectl get pods -n ecom_v1
kubectl get svc -n ecom_v1
kubectl get ingress -n ecom_v1
```

### View Logs
```bash
# Gateway logs
kubectl logs -f deployment/gateway -n ecom_v1

# Service discovery logs
kubectl logs -f deployment/servicediscovery -n ecom_v1

# All pods logs
kubectl logs -l app=gateway -n ecom_v1
```

### Access Minikube Dashboard
```bash
minikube dashboard
```

### Describe Resources
```bash
# Get detailed info about a pod
kubectl describe pod <pod-name> -n ecom_v1

# Get service details
kubectl describe svc gateway -n ecom_v1
```

## 🔍 Service Interconnection

The services are interconnected as follows:

```
Gateway (8080)
├── User Service (8081)
├── Product Service (8082)
├── Cart Service (8083)
├── Order Service (8084)
├── Payment Service (8085)
├── Notification Service (8086)
└── OAuth Server (8087)

Infrastructure:
├── Service Discovery (8761)
├── MySQL (3306)
├── Redis (6379)
├── Kafka (9092)
└── Elasticsearch (9200)
```

## 🧪 Testing the API

### Test Health Endpoints
```bash
# Gateway health
curl http://$(minikube ip):8080/actuator/health

# Individual services
curl http://$(minikube ip):8081/actuator/health  # User service
curl http://$(minikube ip):8082/actuator/health  # Product service
```

### Test API Endpoints
```bash
# Get products
curl http://api.ecom.local/api/v1/products

# Get users
curl http://api.ecom.local/api/v1/users

# Create order (with authentication)
curl -X POST http://api.ecom.local/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId": 1, "items": [{"productId": 1, "quantity": 2}]}'
```

## 🧹 Cleanup

To clean up the deployment:

```bash
# Delete all resources
kubectl delete namespace ecom_v1

# Stop Minikube
minikube stop

# Delete Minikube cluster
minikube delete
```

## 🆘 Troubleshooting

### Common Issues

1. **Pods not starting**: Check resource limits and image availability
2. **Service discovery issues**: Ensure Eureka server is running first
3. **Database connection issues**: Verify MySQL deployment and credentials
4. **Network connectivity**: Check service names and ports

### Useful Commands

```bash
# Check pod events
kubectl describe pod <pod-name> -n ecom_v1

# Check service endpoints
kubectl get endpoints -n ecom_v1

# Check ingress status
kubectl describe ingress ecom-ingress -n ecom_v1

# Access Minikube shell
minikube ssh

# View Minikube logs
minikube logs
```

## 📚 Additional Resources

- [Detailed Deployment Guide](docker-k8s-deployment-guide.md)
- [API Documentation](docs/)
- [Kubernetes Manifests](k8s-deployments/)
- [Service Architecture](tr.md)

---

**Happy Deploying! 🚀** 