package com.services.userservice;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.test.annotation.Commit;

// @SpringBootTest
@SpringBootTest(properties = {
        "server.port=9001",
        "spring.datasource.url=jdbc:mysql://localhost:3306/user_db",
        "spring.datasource.username=turjoysaha",
        "spring.datasource.password=Iam@007",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=update",
        "eureka.client.service-url.defaultZone=http://localhost:8761/eureka/"
})
class UserserviceApplicationTests {

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @Commit
    public void registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("spa-client")
                .redirectUri("http://127.0.0.1:8080/")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read")
                .scope("write")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        registeredClientRepository.save(oidcClient);

        // return new InMemoryRegisteredClientRepository(oidcClient);
    }

}
