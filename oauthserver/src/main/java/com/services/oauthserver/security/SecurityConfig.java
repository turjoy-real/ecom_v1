package com.services.oauthserver.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.services.oauthserver.models.User;
import com.services.oauthserver.security.models.CustomUserDetails;
import com.services.oauthserver.services.SocialLoginService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

@Configuration
public class SecurityConfig {

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
                                .authorizationServer();

                http
                                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                                .with(authorizationServerConfigurer, (authorizationServer) -> authorizationServer
                                                .oidc(withDefaults()))
                                .authorizeHttpRequests((authorize) -> authorize
                                                .requestMatchers("/oauth2/jwks", "/oauth2/authorization/**").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling((exceptions) -> exceptions
                                                .defaultAuthenticationEntryPointFor(
                                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                                .cors(withDefaults())
                                .oauth2ResourceServer(config -> config.jwt(withDefaults()));

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain clientAppSecurityFilterChain(HttpSecurity http, SocialLoginService socialLoginService)
                        throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", // allow base URL
                                                                "/index.html", // allow main HTML
                                                                "/script.js",
                                                                "/login", "/login/**", "/api/users/open/**",
                                                                "/api/users/signup", "/html/**",
                                                                "/api/users/logout",
                                                                "/actuator/**",
                                                                "/oauth2/authorization/**", // Allow OAuth2 authorization endpoints
                                                                "/oauth2/success", // Allow OAuth2 success endpoint
                                                                "/oauth2/failure") // Allow OAuth2 failure endpoint

                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/roles/**").hasRole("ADMIN") // allow
                                                                                                                   // role
                                                                                                                   // API
                                                .anyRequest().authenticated())
                                .formLogin(withDefaults())
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(socialLoginService))
                                                .successHandler((request, response, authentication) -> {
                                                        // Redirect to our custom success endpoint
                                                        response.sendRedirect("/oauth2/success");
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        // Redirect to our custom failure endpoint
                                                        response.sendRedirect("/oauth2/failure?error=" + exception.getMessage());
                                                }))
                                .logout(logout -> logout
                                                .logoutUrl("/api/users/logout") // Ensure this matches your logout
                                                                                // endpoint
                                                .invalidateHttpSession(true) // Invalidate the HTTP session
                                                .clearAuthentication(true) // Clear the security context
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        // Redirect to a safe page after logout, e.g., login page
                                                        response.sendRedirect("/?logout=success");
                                                }))
                                .cors(withDefaults())
                                .build();
        }

        @Bean
        public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
                return (context) -> {
                        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                                context.getClaims().claims((claims) -> {
                                        Set<String> roles = AuthorityUtils
                                                        .authorityListToSet(context.getPrincipal().getAuthorities())
                                                        .stream()
                                                        .map(c -> c.replaceFirst("^ROLE_", ""))
                                                        .collect(Collectors.collectingAndThen(Collectors.toSet(),
                                                                        Collections::unmodifiableSet));
                                        Object principal = context.getPrincipal().getPrincipal();

                                        if (principal instanceof CustomUserDetails customUser) {
                                                User user = customUser.getUser();

                                                claims.put("sub", user.getId().toString()); // ✅ UUID as subject
                                                claims.put("user_email", user.getEmail()); // ✅ Optional email
                                                claims.put("roles", roles);
                                        }
                                });
                        }
                };
        }

    


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
        public JWKSource<SecurityContext> jwkSource() {
        try {
                RSAPublicKey publicKey = (RSAPublicKey) KeyLoader.loadPublicKey("public.pem");
                RSAPrivateKey privateKey = (RSAPrivateKey) KeyLoader.loadPrivateKey("private.pem");
                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("your-key-id") // Use a fixed key ID or compute from the key
                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
        } catch (Exception e) {
                throw new IllegalStateException("Failed to load RSA keys", e);
        }
}

        @Bean
        public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        }

     

        @Bean
        public CommandLineRunner registerClient(RegisteredClientRepository repository,
                        PasswordEncoder passwordEncoder) {
                return args -> {
                        String clientId1 = "spa-client";
                        RegisteredClient existing1 = repository.findByClientId(clientId1);
                        if (existing1 != null) {
                                System.out.println("✅ Client already registered: " + clientId1);
                        } else {
                                RegisteredClient registeredClient1 = RegisteredClient
                                                .withId(UUID.randomUUID().toString())
                                                .clientId("spa-client")
                                                .redirectUri("http://localhost:9001/")
                                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                                                .scope(OidcScopes.OPENID)
                                                .scope(OidcScopes.PROFILE)
                                                .scope("read")
                                                .scope("write")
                                                .scope("offline_access")
                                                .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(true).build())
                                                .tokenSettings(TokenSettings.builder()
                                                                .accessTokenTimeToLive(Duration.ofMinutes(1800))
                                                                .refreshTokenTimeToLive(Duration.ofDays(30))
                                                                .reuseRefreshTokens(false)
                                                                .build())
                                                .build();
                                repository.save(registeredClient1);
                        }

                        // Register paymentservice client for interservice JWT auth
                        String paymentClientId = "paymentservice";
                        String paymentClientSecret = passwordEncoder.encode("change-this-to-a-strong-secret");
                        RegisteredClient existingPayment = repository.findByClientId(paymentClientId);
                        if (existingPayment == null) {
                                RegisteredClient paymentClient = RegisteredClient
                                        .withId(UUID.randomUUID().toString())
                                        .clientId(paymentClientId)
                                        .clientSecret(paymentClientSecret)
                                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                        .scope("internal.webhook")
                                        .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(60))
                                                .build())
                                        .build();
                                repository.save(paymentClient);
                                System.out.println("✅ OAuth2 client registered: " + paymentClientId);
                        } else {
                                System.out.println("✅ Client already registered: " + paymentClientId);
                        }
                };
        }

}
