# Technical Implementation Details

## 1. Authentication & Authorization System (Implemented)

### 1.1 OAuth2 with PKCE Implementation

#### 1.1.1 Why PKCE?

PKCE (Proof Key for Code Exchange) was chosen over traditional email/password authentication for several critical security reasons:

1. **Enhanced Security Against Token Interception**

   - Prevents authorization code interception attacks in public clients
   - Uses code verifier and challenge mechanism
   - Eliminates need for client secrets in browser-based applications

2. **Industry Best Practices**
   - Recommended by OAuth 2.0 Security Best Current Practice [RFC 9126](https://datatracker.ietf.org/doc/html/rfc9126)
   - Endorsed by OAuth 2.0 for Browser-Based Apps [RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252)
   - Aligns with OWASP's recommendations

The PKCE flow works as follows:

1. Client generates a code verifier (random string)
2. Client creates a code challenge by hashing the verifier
3. Authorization request includes the code challenge
4. Token request includes the original code verifier
5. Server verifies the challenge matches the verifier

This prevents authorization code interception attacks, even in public clients like browser-based applications.

#### 1.1.2 Implementation Details

```java
@Configuration
public class SecurityConfig {
    @Bean
    ClientSettings clientSettings() {
        return ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true) // PKCE enabled
                .build();
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer());

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                new OAuth2AccessTokenGenerator(),
                new OAuth2RefreshTokenGenerator());
    }
}
```

The configuration above:

- Enables PKCE for all clients
- Requires explicit consent for authorization
- Configures JWT token generation with custom claims
- Sets up both access and refresh token generation

### 1.2 Token Management

#### 1.2.1 Token Expiration Settings

```java
.tokenSettings(TokenSettings.builder()
    .accessTokenTimeToLive(Duration.ofMinutes(15))
    .refreshTokenTimeToLive(Duration.ofDays(30))
    .reuseRefreshTokens(false)
    .build())
```

Token expiration strategy:

- Access tokens expire after 15 minutes for security
- Refresh tokens last 30 days for user convenience
- Refresh tokens are single-use to prevent token theft
- New refresh token issued with each use

#### 1.2.2 JWT Token Customization

```java
@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
    return (context) -> {
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            context.getClaims().claims((claims) -> {
                Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                        .stream()
                        .map(c -> c.replaceFirst("^ROLE_", ""))
                        .collect(Collectors.collectingAndThen(Collectors.toSet(),
                                Collections::unmodifiableSet));
                claims.put("roles", roles);
            });
        }
    };
}
```

JWT token customization:

- Adds user roles to access tokens
- Removes "ROLE\_" prefix for cleaner role names
- Makes roles immutable in the token
- Only customizes access tokens, not refresh tokens

### 1.3 Role-Based Access Control (RBAC)

#### 1.3.1 Role Entity

```java
@Entity
@Table(name = "roles")
public class Role extends BaseModel {
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}
```

Role management:

- Roles are stored in a separate table
- Many-to-many relationship with users
- Role names must be unique
- Extends BaseModel for common fields

#### 1.3.2 Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/roles").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
}
```

Security rules:

- Public access to API documentation
- Role management restricted to admins
- All other endpoints require authentication
- JWT-based resource server configuration

## 2. Address Management System (Implemented)

### 2.1 Data Model

```java
@Entity
@Table(name = "addresses")
public class Address extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private boolean isDefault;

    private String label;
}
```

Address model features:

- Lazy loading of user relationship
- Required fields for address components
- Optional label for address identification
- Default address flag for primary address

### 2.2 Security Implementation

#### 2.2.1 Controller Level Security

```java
@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @GetMapping
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }
}
```

Controller security:

- Role-based access control
- User ID verification
- Method-level security annotations
- Proper response entity wrapping

#### 2.2.2 Service Level Security

```java
@Service
public class AddressService {
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this address");
        }
        // ... rest of the implementation
    }
}
```

Service layer security:

- Transactional operations
- Entity existence validation
- User ownership verification
- Proper exception handling

## 3. Notification System (Implemented)

### 3.1 Email Verification

#### 3.1.1 Token Management

```java
@Entity
public class EmailVerificationToken extends BaseModel {
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiry;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiry);
    }
}
```

Email verification features:

- Secure token generation
- One-to-one user relationship
- Token expiration handling
- Eager loading for immediate access

#### 3.1.2 Kafka Integration

```java
@Service
public class EmailEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendVerificationEmail(String to, String link) {
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setTo(to);
        event.setSubject("Verify your email");
        event.setVerificationLink(link);
        kafkaTemplate.send("email-verification", to + " // " + link);
    }
}
```

Event-driven email system:

- Asynchronous email processing
- Decoupled notification service
- Event-based communication
- Scalable message handling

### 3.2 Password Reset

```java
@Service
public class PasswordResetService {
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("No user with this email"));

        tokenRepo.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(resetToken);

        String link = baseUrl + "/reset-password.html?token=" + token;
        emailEventProducer.sendPasswordResetEmail(user.getEmail(), link);
    }
}
```

Password reset features:

- Secure token generation
- Token expiration
- Previous token invalidation
- Email notification

## 4. Product Catalog Module (Implemented)

The product catalog module is fully implemented with the following features:

### 4.1 Core Features

- Product CRUD operations
- Category management
- Advanced search capabilities
- Stock management
- Redis caching for performance

### 4.2 Search Features

- Full-text search
- Category-based search
- Brand-based search
- Price range search
- Stock availability search
- Multi-criteria search

### 4.3 Security

- Role-based access control
- JWT authentication
- Public access for product browsing
- Admin-only access for product management

## 5. Cart Module (Implemented)

The shopping cart module is implemented with:

### 5.1 Core Features

- Cart management (add, remove, update)
- Real-time stock verification
- Cart persistence
- Redis caching
- MongoDB storage

### 5.2 Integration

- Product service integration
- User service integration
- Stock verification
- Price calculation

## 6. Order Management Module (Implemented)

The order management system includes:

### 6.1 Core Features

- Order creation
- Order status management
- Payment status tracking
- Order history
- Order cancellation

### 6.2 Integration

- Product service integration
- User service integration
- Address service integration
- Payment service integration

## 7. Payment Module (Partially Implemented)

The payment system is partially implemented with:

### 7.1 Implemented Features

- Razorpay integration
- Payment link generation
- Basic payment status tracking
- Webhook handling

### 7.2 Pending Features

- Multiple payment gateway support
- Refund processing
- Payment method management
- Transaction history
- Payment analytics

## Pending Tasks and Future Implementations

### 1. Payment Module Enhancements

- [ ] **Multiple Payment Gateways**
  - Integrate Stripe
  - Add PayPal
  - Implement local payment methods
- [ ] **Advanced Payment Features**
  - Implement refund processing
  - Add payment method management
  - Create payment analytics
  - Add transaction history

### 2. Analytics and Monitoring

- [ ] **User Analytics**
  - Implement user behavior tracking
  - Add conversion analytics
  - Implement A/B testing
- [ ] **System Monitoring**
  - Add performance metrics
  - Implement error tracking
  - Add usage analytics

### 3. Performance Optimizations

- [ ] **Caching Layer**
  - Implement Redis caching for frequently accessed data
  - Add response caching with proper invalidation
  - Implement cache warming strategies
- [ ] **Database Optimization**
  - Add database indexing for common queries
  - Implement query optimization with explain plans
  - Add database sharding for horizontal scaling

### 4. Testing and Quality Assurance

- [ ] **Test Coverage**
  - Add unit tests for new features
  - Implement integration tests
  - Add performance tests
- [ ] **Quality Checks**
  - Implement code quality gates
  - Add security scanning
  - Implement dependency checks

### 5. Documentation and Developer Experience

- [ ] **API Documentation**
  - Generate OpenAPI documentation
  - Add API usage examples
  - Create integration guides
- [ ] **Development Tools**
  - Add development environment setup scripts
  - Implement local development with Docker
  - Create debugging and troubleshooting guides

### 6. Business Features

- [ ] **Inventory Management**
  - Implement stock tracking
  - Add low stock alerts
  - Create inventory reports
- [ ] **Customer Service**
  - Add ticket management system
  - Implement live chat support
  - Create knowledge base
- [ ] **Marketing Tools**
  - Add email campaign management
  - Implement discount system
  - Create promotional tools
