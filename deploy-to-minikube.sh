#!/bin/bash

# E-Commerce Microservices Deployment Script for Minikube
# This script automates the Docker build and Kubernetes deployment process

set -e

echo "🚀 Starting E-Commerce Microservices Deployment to Minikube"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker Desktop."
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install kubectl."
        exit 1
    fi
    
    if ! command -v minikube &> /dev/null; then
        print_error "Minikube is not installed. Please install Minikube."
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven."
        exit 1
    fi
    
    print_status "All prerequisites are satisfied!"
}

# Start Minikube
start_minikube() {
    print_status "Starting Minikube..."
    
    if ! minikube status &> /dev/null; then
        minikube start --cpus=4 --memory=8192 --disk-size=20g
    else
        print_status "Minikube is already running"
    fi
    
    # Enable ingress addon
    minikube addons enable ingress
    
    # Configure Docker to use Minikube's Docker daemon
    eval $(minikube docker-env)
    
    print_status "Minikube is ready!"
}

# Build all services
build_services() {
    print_status "Building all services with Maven..."
    
    # First build and install the common module to local repository
    print_status "Building common module..."
    cd common-modules
    mvn clean install -DskipTests
    cd ..
    
    services=("userservice" "productservice" "cartservice" "orderservice" "paymentservice" "notificationservice" "oauthserver" "gateway" "servicediscovery")
    
    for service in "${services[@]}"; do
        print_status "Building $service..."
        cd "$service"
        mvn clean package -DskipTests
        cd ..
    done
    
    print_status "All services built successfully!"
}

# Build Docker images
build_docker_images() {
    print_status "Building Docker images..."
    
    services=("userservice" "productservice" "cartservice" "orderservice" "paymentservice" "notificationservice" "oauthserver" "gateway" "servicediscovery")
    
    for service in "${services[@]}"; do
        print_status "Building Docker image for $service..."
        docker build -t "$service:latest" "$service/"
    done
    
    print_status "All Docker images built successfully!"
}

# Deploy to Kubernetes
deploy_to_kubernetes() {
    print_status "Deploying to Kubernetes..."
    
    # Create namespace
    kubectl apply -f k8s-deployments/namespace.yaml
    
    # Deploy infrastructure services
    print_status "Deploying infrastructure services..."
    kubectl apply -f k8s-deployments/mysql/
    kubectl apply -f k8s-deployments/redis/
    kubectl apply -f k8s-deployments/kafka/
    kubectl apply -f k8s-deployments/elasticsearch/
    
    # Wait for infrastructure to be ready
    print_status "Waiting for infrastructure services to be ready..."
    kubectl wait --for=condition=ready pod -l app=mysql --timeout=300s -n ecom_v1
    kubectl wait --for=condition=ready pod -l app=redis --timeout=300s -n ecom_v1
    kubectl wait --for=condition=ready pod -l app=kafka --timeout=300s -n ecom_v1
    
    # Deploy microservices in order
    print_status "Deploying microservices..."
    
    # 1. Service Discovery first
    kubectl apply -f k8s-deployments/servicediscovery/
    kubectl wait --for=condition=ready pod -l app=servicediscovery --timeout=300s -n ecom_v1
    
    # 2. OAuth Server
    kubectl apply -f k8s-deployments/oauthserver/
    
    # 3. Core services
    kubectl apply -f k8s-deployments/userservice/
    kubectl apply -f k8s-deployments/productservice/
    kubectl apply -f k8s-deployments/cartservice/
    kubectl apply -f k8s-deployments/orderservice/
    kubectl apply -f k8s-deployments/paymentservice/
    kubectl apply -f k8s-deployments/notificationservice/
    
    # 4. Gateway last
    kubectl apply -f k8s-deployments/gateway/
    
    # Apply ConfigMaps
    kubectl apply -f k8s-deployments/configmaps.yaml
    
    # Apply Ingress
    kubectl apply -f k8s-deployments/ingress.yaml
    
    print_status "All services deployed successfully!"
}

# Wait for all pods to be ready
wait_for_pods() {
    print_status "Waiting for all pods to be ready..."
    
    kubectl wait --for=condition=ready pod -l app=gateway --timeout=600s -n ecom_v1
    
    print_status "All pods are ready!"
}

# Show deployment status
show_status() {
    print_status "Deployment Status:"
    echo ""
    kubectl get pods -n ecom_v1
    echo ""
    kubectl get svc -n ecom_v1
    echo ""
    kubectl get ingress -n ecom_v1
}

# Get access information
show_access_info() {
    print_status "Access Information:"
    echo ""
    
    # Get Minikube IP
    MINIKUBE_IP=$(minikube ip)
    echo "Minikube IP: $MINIKUBE_IP"
    
    # Get Gateway service info
    GATEWAY_PORT=$(kubectl get svc gateway -n ecom_v1 -o jsonpath='{.spec.ports[0].port}')
    echo "Gateway Port: $GATEWAY_PORT"
    
    echo ""
    echo "Access URLs:"
    echo "Gateway API: http://$MINIKUBE_IP:$GATEWAY_PORT"
    echo "Eureka Dashboard: http://$MINIKUBE_IP:8761"
    echo ""
    echo "To add host entries for ingress:"
    echo "echo \"$MINIKUBE_IP api.ecom.local ecom.local\" | sudo tee -a /etc/hosts"
    echo ""
    echo "Then access via:"
    echo "http://api.ecom.local"
    echo "http://ecom.local"
}

# Main execution
main() {
    print_status "Starting E-Commerce Microservices Deployment"
    
    check_prerequisites
    start_minikube
    build_services
    build_docker_images
    deploy_to_kubernetes
    wait_for_pods
    show_status
    show_access_info
    
    print_status "Deployment completed successfully! 🎉"
}

# Run main function
main "$@" 