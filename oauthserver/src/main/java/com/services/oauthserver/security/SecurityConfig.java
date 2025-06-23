package com.services.oauthserver.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.services.oauthserver.models.User;
import com.services.oauthserver.security.models.CustomUserDetails;

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
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

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
        public SecurityFilterChain clientAppSecurityFilterChain(HttpSecurity http)
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
                                                                "/actuator/**")

                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/roles/**").hasRole("ADMIN") // allow
                                                                                                                   // role
                                                                                                                   // API
                                                .anyRequest().authenticated())
                                .formLogin(withDefaults())
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
        public CommandLineRunner registerClient(RegisteredClientRepository repository,
                        PasswordEncoder passwordEncoder) {
                return args -> {
                        String clientId1 = "spa-client";
                        // String clientId2 = "oidc-client";

                        RegisteredClient existing1 = repository.findByClientId(clientId1);
                        // RegisteredClient existing2 = repository.findByClientId(clientId2);
                        if (existing1 != null) {
                                System.out.println("✅ Client already registered: " + clientId1);
                                return;
                        }

                        if (existing1 == null) {

                                RegisteredClient registeredClient1 = RegisteredClient
                                                .withId(UUID.randomUUID().toString())
                                                .clientId("spa-client")
                                                .redirectUri("http://localhost:9001/") // hosted inside the auth server
                                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // ✅ must
                                                                                                              // allow
                                                                                                              // this
                                                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                                                .scope(OidcScopes.OPENID)
                                                .scope(OidcScopes.PROFILE)
                                                .scope("read")
                                                .scope("write")
                                                .scope("offline_access") // ✅ for refresh token
                                                .clientSettings(ClientSettings.builder()
                                                                .requireProofKey(true) // PKCE
                                                                .requireAuthorizationConsent(true)
                                                                .build())
                                                .tokenSettings(TokenSettings.builder()
                                                                .accessTokenTimeToLive(Duration.ofMinutes(60))
                                                                .refreshTokenTimeToLive(Duration.ofDays(30))
                                                                .reuseRefreshTokens(false) // new refresh token each
                                                                                           // time
                                                                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                                                                .build())
                                                .build();

                                repository.save(registeredClient1);
                        }

                        System.out.println("✅ OAuth2 client registered: " + clientId1);
                };
        }

}
