# Launch Configuration Guide

This guide explains how to set up and use the VS Code launch configurations for the E-Commerce Microservices Platform.

## Overview

The `launch_copy.json` file provides a template for configuring VS Code debug configurations for all microservices. This allows you to run and debug services directly from VS Code with proper environment variables.

## Quick Setup

### 1. Copy the Template

Copy the `launch_copy.json` file to your `.vscode` directory:

```bash
cp docs/launch_copy.json .vscode/launch.json
```

### 2. Configure Environment Variables

Replace the placeholder values in the launch configuration with your actual credentials:

#### Database Configuration
```json
{
    "DB_URL": "mysql://localhost:3306/your_database",
    "DB_USER": "your_username",
    "DB_PASS": "your_password"
}
```

#### External Services
```json
{
    "REDIS_URL": "redis://localhost:6379",
    "EUREKA_SERVER": "http://localhost:8761/eureka/",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

## Service-Specific Configurations

### 1. Service Discovery (Eureka)
- **Port**: 8761 (default)
- **No environment variables required**
- **Start first** - All other services depend on this

### 2. OAuth Server
```json
{
    "DB_URL": "mysql://localhost:3306/user_db",
    "DB_USER": "your_username",
    "DB_PASS": "your_password",
    "SERVER_PORT": "9001",
    "EUREKA_SERVER": "http://localhost:8761/eureka",
    "GOOGLE_CLIENT_ID": "your_google_client_id.apps.googleusercontent.com",
    "GOOGLE_CLIENT_SECRET": "your_google_client_secret",
    "ADMIN_EMAIL": "admin@yourdomain.com",
    "ADMIN_PASSWORD": "your_admin_password"
}
```

**Required Setup:**
- Generate RSA key pair for JWT signing
- Configure Google OAuth credentials
- Set up admin user credentials

### 3. User Service
```json
{
    "DB_URL": "mysql://localhost:3306/user_db",
    "DB_USER": "your_username",
    "DB_PASS": "your_password",
    "SERVER_PORT": "9002",
    "EUREKA_SERVER": "http://localhost:8761/eureka",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

### 4. Product Service
```json
{
    "DB_URL": "mysql://localhost:3306/product_db",
    "DB_USER": "your_username",
    "DB_PASS": "your_password",
    "ELASTIC_PASS": "your_elasticsearch_password",
    "ELASTIC_URL": "http://localhost:9200",
    "ELASTIC_API_KEY": "your_elasticsearch_api_key",
    "ELASTIC_USER": "elastic",
    "PORT": "8000",
    "EUREKA_SERVER": "http://localhost:8761/eureka/",
    "REDIS_URL": "redis://localhost:6379",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

**Required Setup:**
- Elasticsearch instance running
- Redis cache instance
- Product database with Flyway migrations

### 5. Cart Service
```json
{
    "PORT": "7001",
    "EUREKA_SERVER": "http://localhost:8761/eureka/",
    "REDIS_URL": "redis://localhost:6379",
    "MONGO_DB_URL": "mongodb+srv://your_username:your_password@your_cluster.mongodb.net/",
    "MONGO_DB_NAME": "cart",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

**Required Setup:**
- MongoDB Atlas cluster or local MongoDB
- Redis cache instance

### 6. Order Service
```json
{
    "DB_URL": "mysql://localhost:3306/order_db",
    "DB_USER": "your_username",
    "DB_PASS": "your_password",
    "EUREKA_SERVER": "http://localhost:8761/eureka/",
    "SERVER_PORT": "6000",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

### 7. Payment Service
```json
{
    "RAZORPAY_KEY_ID": "your_razorpay_key_id",
    "RAZORPAY_KEY_SECRET": "your_razorpay_key_secret",
    "RAZORPAY_WEBHOOK_SECRET": "your_webhook_secret",
    "SERVER_PORT": "5002",
    "DB_PASS": "your_password",
    "DB_URL": "mysql://localhost:3306/transaction_db",
    "DB_USER": "your_username",
    "EUREKA_SERVER": "http://localhost:8761/eureka/",
    "JWT_ISSUER_URI": "http://localhost:9001"
}
```

**Required Setup:**
- Razorpay account and API credentials
- Transaction database

### 8. Notification Service
```json
{
    "SMTP_USERNAME": "your_email@gmail.com",
    "SMTP_PASSWORD": "your_app_password",
    "SMTP_HOST": "smtp.gmail.com",
    "SMTP_PORT": "587",
    "SERVER_PORT": "5001"
}
```

**Required Setup:**
- Gmail account with App Password
- Or configure other SMTP providers

### 9. Gateway
- **No environment variables required**
- **Start last** - Acts as the API gateway

## External Service Setup

### 1. MySQL Database
```bash
# Install MySQL
sudo apt-get install mysql-server

# Create databases
mysql -u root -p
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE transaction_db;

# Create user
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON *.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Redis Cache
```bash
# Install Redis
sudo apt-get install redis-server

# Start Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### 3. Elasticsearch
```bash
# Download and install Elasticsearch
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.17.3-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.17.3-linux-x86_64.tar.gz
cd elasticsearch-8.17.3

# Start Elasticsearch
./bin/elasticsearch
```

### 4. MongoDB Atlas
1. Create MongoDB Atlas account
2. Create a cluster
3. Get connection string
4. Replace in cart service configuration

### 5. Google OAuth
1. Go to Google Cloud Console
2. Create OAuth 2.0 credentials
3. Add authorized redirect URIs
4. Copy Client ID and Secret

### 6. Razorpay
1. Create Razorpay account
2. Get API keys from dashboard
3. Configure webhook endpoints

## Running Services

### 1. Start Infrastructure Services
```bash
# Start MySQL
sudo systemctl start mysql

# Start Redis
sudo systemctl start redis-server

# Start Elasticsearch
./elasticsearch-8.17.3/bin/elasticsearch
```

### 2. Start Services in Order
1. **Service Discovery** - Right-click → "Start Debugging"
2. **OAuth Server** - Right-click → "Start Debugging"
3. **User Service** - Right-click → "Start Debugging"
4. **Product Service** - Right-click → "Start Debugging"
5. **Cart Service** - Right-click → "Start Debugging"
6. **Order Service** - Right-click → "Start Debugging"
7. **Payment Service** - Right-click → "Start Debugging"
8. **Notification Service** - Right-click → "Start Debugging"
9. **Gateway** - Right-click → "Start Debugging"

### 3. Verify Services
- Check Eureka Dashboard: `http://localhost:8761`
- Check Gateway: `http://localhost:8080`
- Check OAuth Server: `http://localhost:9001`

## Debugging

### Breakpoints
- Set breakpoints in your Java code
- Use VS Code's debugging features
- Inspect variables and call stack

### Logs
- View console output in VS Code
- Check application logs
- Monitor service health

### Common Issues

#### 1. Port Conflicts
```bash
# Check if ports are in use
netstat -tulpn | grep :8080
netstat -tulpn | grep :9001
```

#### 2. Database Connection Issues
```bash
# Test MySQL connection
mysql -u your_username -p -h localhost

# Check MySQL status
sudo systemctl status mysql
```

#### 3. Service Discovery Issues
- Ensure all services use consistent hostnames
- Check Eureka server is running
- Verify service registration

#### 4. Environment Variable Issues
- Double-check all placeholder values are replaced
- Ensure no extra spaces or quotes
- Verify JSON syntax is valid

## Security Considerations

### 1. Environment Variables
- Never commit real credentials to version control
- Use environment-specific configurations
- Consider using `.env` files for local development

### 2. Database Security
- Use strong passwords
- Limit database user privileges
- Enable SSL connections in production

### 3. API Security
- Use HTTPS in production
- Implement proper authentication
- Validate all inputs

## Production Deployment

For production deployment, consider:
- Using Kubernetes Secrets for sensitive data
- Implementing proper logging and monitoring
- Setting up health checks and auto-scaling
- Using external service providers for databases and caches

## Troubleshooting

### Service Won't Start
1. Check all dependencies are running
2. Verify environment variables
3. Check port availability
4. Review application logs

### Database Connection Failed
1. Verify MySQL is running
2. Check credentials
3. Ensure database exists
4. Test connection manually

### Service Discovery Issues
1. Check Eureka server is running
2. Verify service configuration
3. Check network connectivity
4. Review service logs

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [VS Code Java Extension](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Redis Documentation](https://redis.io/documentation)
- [Elasticsearch Documentation](https://www.elastic.co/guide/index.html) 