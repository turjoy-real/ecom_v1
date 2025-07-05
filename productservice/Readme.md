# Product Service

A microservice responsible for managing product catalog, inventory, and search functionality in the e-commerce platform.

## Features

- Product catalog management
- Inventory tracking
- Product search and filtering
- Category management
- Product images and attributes
- Elasticsearch integration for search
- Redis caching for performance

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 8.0+

### Running the Service

1. **Start dependencies:**
   ```bash
   # Start MySQL
   docker run -d --name mysql-product -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=product_db -p 3306:3306 mysql:8.0
   
   # Start Redis
   docker run -d --name redis-product -p 6379:6379 redis:6.0
   
   # Start Elasticsearch
   docker run -d --name elasticsearch-product -e "discovery.type=single-node" -p 9200:9200 elasticsearch:8.0
   ```

2. **Configure database:**
   ```bash
   # Update application.properties with your database credentials
   spring.datasource.url=jdbc:mysql://localhost:3306/product_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the service:**
   ```bash
   mvn spring-boot:run
   ```

The service will be available at `http://localhost:8080`

## API Endpoints

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/search?q={query}` - Search products

### Categories
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

## Configuration

### Database
- **Type:** MySQL 8.0
- **Migration:** Flyway
- **Connection Pool:** HikariCP

### Cache
- **Type:** Redis
- **TTL:** Configurable per cache type
- **Serialization:** JSON

### Search
- **Engine:** Elasticsearch
- **Index:** products
- **Mapping:** Auto-configured

## Documentation

- **[Database Migration Guide](db_migration_guide.md)** - Database setup, migration scripts, and schema management
- **[Testing Guide](testing_guide.md)** - Testing strategy, test types, and troubleshooting

## Development

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Docker Build
```bash
docker build -t productservice .
```

## Monitoring

### Health Checks
- `GET /actuator/health` - Application health
- `GET /actuator/health/db` - Database health
- `GET /actuator/health/redis` - Redis health
- `GET /actuator/health/elasticsearch` - Elasticsearch health

### Metrics
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running
   - Check credentials in application.properties
   - Ensure database exists

2. **Redis Connection Failed**
   - Verify Redis is running
   - Check Redis configuration
   - Ensure network connectivity

3. **Elasticsearch Connection Failed**
   - Verify Elasticsearch is running
   - Check Elasticsearch configuration
   - Ensure cluster health is green

### Logs
```bash
# View application logs
tail -f logs/application.log

# View specific service logs
grep "ProductService" logs/application.log
```

## Contributing

1. Follow the existing code style
2. Add tests for new features
3. Update documentation as needed
4. Ensure all tests pass before submitting

## License

This project is licensed under the MIT License. 