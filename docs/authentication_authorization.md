# Authentication and Authorization System

## 🔹 Objective

Implement a secure and scalable user authentication and authorization system within a microservices architecture, supporting both email/password login and role-based access control.

## 🔹 Why PKCE Over Traditional Authentication?

PKCE (Proof Key for Code Exchange) was chosen over traditional email/password authentication for several critical security reasons:

1. **Enhanced Security Against Token Interception**

   - PKCE prevents authorization code interception attacks in public clients
   - Uses a code verifier and challenge mechanism to ensure the same client that initiated the flow completes it
   - Eliminates the need to store client secrets in browser-based applications

2. **Industry Best Practices**

   - Recommended by OAuth 2.0 Security Best Current Practice (BCP) [RFC 9126](https://datatracker.ietf.org/doc/html/rfc9126)
   - Endorsed by OAuth 2.0 for Browser-Based Apps [RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252)
   - Aligns with OWASP's recommendations for secure authentication

3. **Key Advantages Over Email/Password**

   - No password storage required on the client side
   - Reduced risk of credential theft
   - Better protection against phishing attacks
   - Support for multiple identity providers
   - Easier integration with social login providers

4. **Real-world Implementation Benefits**
   - Simplified token refresh flow
   - Better user experience with SSO capabilities
   - Reduced maintenance overhead
   - Compliance with modern security standards

For more information, refer to:

- [OAuth 2.0 Security Best Current Practice](https://datatracker.ietf.org/doc/html/rfc9126)
- [OAuth 2.0 for Browser-Based Apps](https://datatracker.ietf.org/doc/html/rfc8252)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## 🔹 System Architecture

The system is built using a microservices architecture with three main services:

1. **OAuth Server**: Handles authentication and token management
2. **User Service**: Manages user data and role-based access control
3. **Notification Service**: Handles email communications asynchronously

These services communicate through:

- REST APIs for synchronous operations
- Kafka for asynchronous event processing
- JWT tokens for secure inter-service communication

## 🔹 Subfeatures Developed

### ✅ Authentication (PKCE + OAuth2)

The authentication system implements the OAuth2 protocol with PKCE (Proof Key for Code Exchange) for enhanced security in browser-based applications. This approach prevents token interception attacks that could occur with the implicit grant flow.

**Key Components:**

- Spring Authorization Server for OAuth2 implementation
- PKCE flow for secure browser-based authentication
- JWT token generation and validation
- Role-based claims in tokens

**Implementation Details:**

```java
// OAuth2 Authorization Server Configuration
@Configuration
public class SecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            new OAuth2AuthorizationServerConfigurer();

        http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/oauth2/jwks", "/oauth2/authorization/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(config -> config.jwt(withDefaults()));

        return http.build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims((claims) -> {
                    Set<String> roles = AuthorityUtils.authorityListToSet(
                        context.getPrincipal().getAuthorities())
                        .stream()
                        .map(c -> c.replaceFirst("^ROLE_", ""))
                        .collect(Collectors.toSet());
                    claims.put("roles", roles);
                });
            }
        };
    }
}
```

### ✅ Authorization (RBAC)

The Role-Based Access Control (RBAC) system provides fine-grained access control across the application. It uses a combination of database-stored roles and JWT token claims to enforce access restrictions.

**Key Features:**

- Hierarchical role system (USER, ADMIN, MODERATOR)
- Database-backed role storage
- Method-level security annotations
- Automatic role assignment during user registration
- Token-based role verification

**Implementation Details:**

```java
// Role Entity
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleType name;
}

// Security Configuration
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/roles/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("USER")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }
}
```

### ✅ Microservices Intercommunication

The system uses a combination of synchronous and asynchronous communication patterns to ensure reliable service interaction while maintaining loose coupling.

**Communication Patterns:**

- REST APIs for direct service-to-service communication
- JWT token propagation for authentication
- Kafka for event-driven communication
- Circuit breakers for fault tolerance

**Implementation Details:**

```properties
# Service Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=${JWT_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWK_SET_URI}
```

### ✅ Email Validation

The email validation system uses an event-driven architecture to handle email verification asynchronously, improving the user experience by not blocking the registration process.

**Process Flow:**

1. User registers
2. System generates verification token
3. Event published to Kafka
4. Notification service sends email
5. User clicks verification link
6. System validates token and activates account

**Implementation Details:**

```java
// Kafka Configuration
@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic emailVerificationTopic() {
        return new NewTopic("email-verification", 1, (short) 1);
    }
}

// Email Event Producer
@Service
public class EmailEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendVerificationEmail(String to, String link) {
        kafkaTemplate.send("email-verification", to + " // " + link);
    }
}

// Notification Service Consumer
@Service
public class EmailVerificationConsumer {
    @KafkaListener(topics = "email-verification", groupId = "email-verification-group")
    public void handleEmailVerification(String msg) {
        String[] parts = msg.split(" // ");
        String to = parts[0];
        String link = parts[1];
        emailAdapter.sendMsg(to, "Verify Your Email",
            "Please click the following link to verify your email: " + link, null);
    }
}
```

### ✅ Password Reset

The password reset system implements a secure, time-limited token-based approach to ensure account security while providing a user-friendly password recovery process.

**Security Features:**

- One-time use tokens
- 15-minute expiration
- Secure token generation
- Email-based verification
- Rate limiting on reset requests

**Implementation Details:**

```java
// Password Reset Event Producer
@Service
public class EmailEventProducer {
    public void sendPasswordResetEmail(String to, String link) {
        kafkaTemplate.send("password-reset", to + " // " + link);
    }
}

// Password Reset Consumer
@Service
public class PasswordResetConsumer {
    @KafkaListener(topics = "password-reset", groupId = "password-reset-group")
    public void handlePasswordReset(String msg) {
        String[] parts = msg.split(" // ");
        String to = parts[0];
        String link = parts[1];
        emailAdapter.sendMsg(to, "Reset Your Password",
            "Please click the following link to reset your password: " + link, null);
    }
}
```

## 🔹 Design Decisions

The system's architecture and implementation choices were driven by several key considerations:

1. **Security First**:

   - PKCE implementation for browser security
   - JWT with role claims for authorization
   - Time-limited tokens for sensitive operations

2. **Scalability**:

   - Microservices architecture for independent scaling
   - Event-driven communication for loose coupling
   - Asynchronous email processing

3. **User Experience**:

   - Non-blocking registration process
   - Quick password reset flow
   - Clear error messages and feedback

4. **Maintainability**:
   - Clear separation of concerns
   - Consistent security patterns
   - Well-documented code and configurations

## 🔹 Observations

During development and testing, several key insights emerged:

1. **Security Benefits**:

   - PKCE effectively prevents token interception
   - Role-based annotations simplify access control
   - JWT propagation requires careful key management

2. **Performance Considerations**:

   - Asynchronous email processing improves response times
   - Event-driven architecture reduces service coupling
   - Token validation overhead is minimal

3. **Operational Insights**:
   - Centralized logging is crucial for debugging
   - Service discovery simplifies deployment
   - Configuration management needs careful planning

## 🔹 Future Enhancements

Planned improvements to enhance the system:

1. **Security Enhancements**:

   - Multi-factor authentication (MFA)
   - Account lockout mechanisms
   - Enhanced audit logging

2. **User Experience**:

   - Social login integration
   - Remember-me functionality
   - Session management improvements

3. **Operational Improvements**:
   - Enhanced monitoring and alerting
   - Automated security scanning
   - Performance optimization
