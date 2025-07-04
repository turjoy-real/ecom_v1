package com.services.paymentservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoders;

import static org.springframework.security.config.Customizer.withDefaults;

import org.apache.http.protocol.HTTP;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        String issuerUri;

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/razorpay/**")

                                                .permitAll()
                                                .anyRequest().authenticated())
                                .cors(withDefaults())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/razorpay/**") // <-- Ignore CSRF for
                                                                                             // webhook
                                )
                                .oauth2ResourceServer(oauth2 -> oauth2

                                                .jwt(jwt -> jwt
                                                                .decoder(JwtDecoders.fromIssuerLocation(issuerUri))
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())))

                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // 👈 matches your token claim
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // 👈 required prefix

                JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
                authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

                return authenticationConverter;
        }

}