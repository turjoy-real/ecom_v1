# Database Migration Guide - Product Service

## Overview
This guide covers the database setup, migration scripts, and schema management for the Product Service using Flyway migration tool.

---

## Database Setup

### Prerequisites
- MySQL 8.0 or higher
- Database user with CREATE, ALTER, DROP privileges
- Flyway migration tool (included in Spring Boot)

### Required Maven Dependencies

The following dependencies are required for database migration functionality:

```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Flyway Core -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>11.10.0</version>
</dependency>

<!-- Flyway MySQL Support -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
    <version>11.10.0</version>
</dependency>

<!-- Spring Boot JPA Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database (for testing) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**Note:** The `flyway-mysql` dependency is essential for MySQL-specific features like stored procedures and MySQL-specific SQL syntax support.

### Version Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| Spring Boot | 3.4.3 | Parent POM version |
| Flyway Core | 11.10.0 | Database migration tool |
| Flyway MySQL | 11.10.0 | MySQL-specific support |
| MySQL Connector | Latest | Auto-managed by Spring Boot |
| H2 Database | Latest | For testing only |

**Important:** Keep Flyway Core and Flyway MySQL versions in sync to avoid compatibility issues.

### Database Configuration

The Product Service uses MySQL as its primary database with the following configuration:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/product_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Flyway Migration Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db.migration
```

---

## Migration Scripts

### Location
All migration scripts are located in: `src/main/resources/db.migration/`

### Naming Convention
- Format: `V{version}__{description}.sql`
- Example: `V1__init.sql`

### Current Migration Scripts

#### V1__init.sql
```sql
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `product_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `product_db`;
DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `inventory`;

DROP TABLE IF EXISTS `products`;

CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

---

## Running Migrations

### Automatic Migration (Default)
When the application starts, Flyway automatically runs pending migrations:

```bash
# Start the application - migrations run automatically
mvn spring-boot:run
```

### Manual Migration
To run migrations manually:

```bash
# Using Maven Flyway plugin
mvn flyway:migrate

# Using Flyway CLI
flyway -url=jdbc:${DB_URL} -user=${DB_USER} -password=${DB_PASS} migrate
```

### Maven Plugin Configuration (Optional)

For enhanced Flyway integration with Maven, you can add the Flyway Maven plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>11.10.0</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/product_db</url>
        <user>your_username</user>
        <password>your_password</password>
        <locations>
            <location>classpath:db.migration</location>
        </locations>
    </configuration>
</plugin>
```

This enables additional Maven goals like:
- `mvn flyway:info` - Check migration status
- `mvn flyway:validate` - Validate migrations
- `mvn flyway:repair` - Repair migration state
- `mvn flyway:clean` - Clean database (use with caution)

### Migration Status
Check migration status:

```bash
# Using Maven
mvn flyway:info

# Using Flyway CLI
flyway -url=jdbc:${DB_URL} -user=${DB_USER} -password=${DB_PASS} info
```

---

## Database Schema

### Core Tables

#### products
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| brand | VARCHAR(255) | Product brand |
| description | VARCHAR(255) | Product description |
| image_url | VARCHAR(255) | Product image URL |
| name | VARCHAR(255) | Product name |
| price | DOUBLE | Product price |
| stock_quantity | INT | Available stock quantity |
| category_id | BIGINT | Foreign key to categories table |

#### categories
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| name | VARCHAR(255) | Category name |

---

## Environment-Specific Configuration

### Development
```properties
spring.datasource.url=jdbc:${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true
spring.flyway.locations=classpath:db.migration
```

### Testing
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.flyway.enabled=false
```

### Production
```properties
spring.datasource.url=jdbc:${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true
spring.flyway.locations=classpath:db.migration
```

---

## Best Practices

### Migration Scripts
1. **Never modify existing migrations** - Create new ones instead
2. **Use descriptive names** - Include what the migration does
3. **Test migrations** - Always test on a copy of production data
4. **Version control** - Keep all migration scripts in version control
5. **Rollback planning** - Consider how to rollback changes

### Database Design
1. **Use appropriate indexes** - Index foreign keys and frequently queried columns
2. **Normalize data** - Avoid data duplication
3. **Use constraints** - Foreign keys, unique constraints, not null
4. **Plan for growth** - Consider future requirements

### Current Schema Notes
- **Database:** `product_db` with UTF8MB4 character set
- **Products table:** Includes brand, description, image_url, name, price, stock_quantity, and category_id
- **Categories table:** Simple structure with id and name
- **Foreign Key:** Products reference categories via `FKog2rp4qthbtt2lfyhfo32lsw9` constraint
- **Engine:** InnoDB for transaction support
- **Auto-increment:** Both tables use auto-incrementing primary keys

### Performance
1. **Monitor query performance** - Use EXPLAIN to analyze queries
2. **Optimize indexes** - Add indexes for slow queries
3. **Consider partitioning** - For large tables
4. **Regular maintenance** - Analyze and optimize tables

---

## Troubleshooting

### Common Issues

#### Migration Failed
```bash
# Check migration status
mvn flyway:info

# Check database connection
mysql -u ${DB_USER} -p product_db

# Review error logs
tail -f logs/application.log
```

#### Schema Validation Errors
```bash
# Validate schema
mvn flyway:validate

# Repair if needed
mvn flyway:repair
```

#### Connection Issues
- Verify database is running
- Check credentials in application.properties
- Ensure database exists
- Verify network connectivity

### Recovery Steps

#### Reset Database (Development Only)
```bash
# Drop and recreate database
mysql -u root -p -e "DROP DATABASE IF EXISTS product_db; CREATE DATABASE product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

# Run migrations from scratch
mvn flyway:migrate
```

#### Rollback Migration
```bash
# Rollback to specific version
mvn flyway:migrate -Dflyway.target=1.0

# Or use Flyway CLI
flyway migrate -target=1.0
```

---

## Monitoring and Maintenance

### Migration History
Check migration history table:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Database Health
```sql
-- Check table sizes
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'product_db';

-- Check index usage
SHOW INDEX FROM products;
```

### Backup Strategy
```bash
# Create backup
mysqldump -u ${DB_USER} -p product_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
mysql -u ${DB_USER} -p product_db < backup_file.sql
```

---

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Spring Boot Database Migration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-basic-sql-scripts) 