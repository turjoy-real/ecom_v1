#!/bin/bash

# Test Build Script for E-Commerce Microservices
# This script tests the build process to ensure all dependencies are resolved

set -e

echo "🧪 Testing Build Process"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Test common module build
test_common_module() {
    print_status "Testing common module build..."
    cd common-modules
    if mvn clean install -DskipTests; then
        print_status "✅ Common module built successfully"
    else
        print_error "❌ Common module build failed"
        exit 1
    fi
    cd ..
}

# Test individual service builds
test_service_build() {
    local service=$1
    print_status "Testing $service build..."
    cd "$service"
    if mvn clean package -DskipTests; then
        print_status "✅ $service built successfully"
    else
        print_error "❌ $service build failed"
        exit 1
    fi
    cd ..
}

# Main test execution
main() {
    print_status "Starting build tests..."
    
    # Test common module first
    test_common_module
    
    # Test each service
    services=("userservice" "productservice" "cartservice" "orderservice" "paymentservice" "notificationservice" "oauthserver" "gateway" "servicediscovery")
    
    for service in "${services[@]}"; do
        test_service_build "$service"
    done
    
    print_status "🎉 All builds successful! Ready for Docker image creation."
}

# Run main function
main "$@" 