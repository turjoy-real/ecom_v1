package com.services.userservice.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        OAuth2AuthorizationServerConfigurer configurer = new OAuth2AuthorizationServerConfigurer();
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()); // Enable OpenID Connect 1.0
        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .apply(configurer);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().permitAll())
                .csrf().disable()
                .cors().disable()
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();

    }

    // @Bean
    // public UserDetailsService userDetailsService() {
    // UserDetails userDetails = User.builder()
    // .username("user")
    // .password("$2a$16$AcBmaZLe06Hx5QSL1PVmRev3W3Fuzy..A18THjaUM.AYEcEDoTORC")
    // .roles("USER")
    // .build();
    //
    // return new InMemoryUserDetailsManager(userDetails);
    // }

    // @Bean
    // public RegisteredClientRepository registeredClientRepository() {
    // RegisteredClient oidcClient =
    // RegisteredClient.withId(UUID.randomUUID().toString())
    // .clientId("oidc-client")
    // .clientSecret("{noop}secret")
    // .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    // .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    // .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
    // .redirectUri("https://oauth.pstmn.io/v1/callback")
    // .postLogoutRedirectUri("https://oauth.pstmn.io/v1/callback")
    // .scope(OidcScopes.OPENID)
    // .scope(OidcScopes.PROFILE)
    // .scope("ADMIN")
    // .scope("STUDENT")
    // .scope("MENTOR") // Role
    // .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
    // .build();
    //
    // return new InMemoryRegisteredClientRepository(oidcClient);
    // }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /* symetric and asymetric */

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    // @Bean
    // public OAuth2AuthorizationServerConfigurer authorizationServerConfigurer(
    // OAuth2TokenGenerator<?> tokenGenerator,
    // RegisteredClientRepository registeredClientRepository,
    // OAuth2AuthorizationService authorizationService,
    // AuthenticationManager authenticationManager) {

    // return new OAuth2AuthorizationServerConfigurer<HttpSecurity>()
    // .tokenEndpoint(tokenEndpoint -> tokenEndpoint
    // .accessTokenRequestConverter(new OAuth2PasswordAuthenticationConverter()) //
    // 👈 handles input
    // .authenticationProvider(new OAuth2PasswordAuthenticationProvider(
    // authenticationManager,
    // registeredClientRepository,
    // authorizationService,
    // tokenGenerator))); // 👈 actually supports PASSWORD grant
    // }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims((claims) -> {
                    Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                            .stream()
                            .map(c -> c.replaceFirst("^ROLE_", ""))
                            .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
                    claims.put("roles", roles);
                    claims.put("ScalerRole", "ADMIN");
                });
            }
        };
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer()); // optional roles claim etc.

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                new OAuth2AccessTokenGenerator(),
                new OAuth2RefreshTokenGenerator());
    }

    @Bean
    public CommandLineRunner registerClient(RegisteredClientRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            String clientId = "client";
            RegisteredClient existing = repository.findByClientId(clientId);
            if (existing != null) {
                System.out.println("✅ Client already registered: " + clientId);
                return;
            }

            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode("secret"))
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD) // or AUTHORIZATION_CODE
                    .scope("read")
                    .scope("write")
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .build())
                    .build();

            repository.save(registeredClient);
            System.out.println("✅ OAuth2 client registered: " + clientId);
        };
    }

    // @Bean
    // public OAuth2TokenGenerator<?> tokenGenerator(
    // JwtEncoder jwtEncoder,
    // OAuth2AccessTokenGenerator accessTokenGenerator,
    // OAuth2RefreshTokenGenerator refreshTokenGenerator) {
    // JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    // jwtGenerator.setJwtCustomizer(jwtTokenCustomizer()); // Optional if you want
    // custom claims

    // return new DelegatingOAuth2TokenGenerator(
    // jwtGenerator,
    // accessTokenGenerator,
    // refreshTokenGenerator);
    // }
}
