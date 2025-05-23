package com.services.userservice.services;

import com.services.userservice.exceptions.IncorrectPassword;
import com.services.userservice.exceptions.UnAuthorized;
import com.services.userservice.exceptions.UserAlreadyRegistered;
import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.models.Token;
import com.services.userservice.models.User;
import com.services.userservice.repositories.TokenRepo;
import com.services.userservice.repositories.UserRepo;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.RandomStringUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.oauth2.core.AuthorizationGrantType;
// import org.springframework.security.oauth2.core.OAuth2AccessToken;
// import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
// import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
// import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
// import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
// import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
// import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
// import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
// import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

// import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
// import java.util.Set;

// import org.springframework.security.core.Authentication;

// Ideally should be an interface
@Service
@RequiredArgsConstructor // This is a bean, so we can inject it anywhere we need it.
public class UserService {

    // private final JWTService JWTService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepo userRepository;
    private final TokenRepo tokenRepository;
    // private final AuthenticationManager authManager;

    // private final AuthenticationManager authenticationManager;

    // private final RegisteredClientRepository registeredClientRepository;

    // private final OAuth2AuthorizationService authorizationService;

    // private final OAuth2TokenGenerator<OAuth2AccessToken> tokenGenerator;

    // public UserService(BCryptPasswordEncoder bCryptPasswordEncoder,
    // UserRepo userRepository,
    // TokenRepo tokenRepository, AuthenticationManager authManager) {
    // this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    // this.userRepository = userRepository;
    // this.tokenRepository = tokenRepository;
    // this.authManager = authManager;
    // }
    public User signUp(String name, String email, String password) throws UserAlreadyRegistered {
        // Validation

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyRegistered("User is already registered");
        }

        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setHashedPassword(bCryptPasswordEncoder.encode(password));

        User user = userRepository.save(u);
        // print
        System.err.println("User data: ...");
        System.err.println(user);
        return user;
    }

    public Token login(String email, String password) {
        // Authentication authentication = authManager
        // .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        Optional<User> userOptional = userRepository.findByEmail(email);
        // System.err.println("User data: ...");
        // System.err.println(authentication);
        if (userOptional.isEmpty()) {
            throw new UserNotFound("User not found");
        }

        // RegisteredClient registeredClient =
        // registeredClientRepository.findByClientId("client");
        // if (registeredClient == null) {
        // throw new RuntimeException("OAuth2 Client not found");
        // }

        User user = userOptional.get();

        // OAuth2Authorization authorization =
        // OAuth2Authorization.withRegisteredClient(registeredClient)
        // .principalName(email)
        // .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        // .attribute(Principal.class.getName(), authentication.getPrincipal())
        // .build();

        // OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
        // .registeredClient(registeredClient)
        // .principal(
        // authentication)
        // .authorization(authorization)
        // .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        // .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        // .authorizedScopes(Set.of("read", "write"))
        // .build();
        // System.out.println("🔧 TokenContext principal: " +
        // tokenContext.getPrincipal());
        // System.out.println("🔧 TokenContext scopes: " +
        // tokenContext.getAuthorizedScopes());
        // System.out.println("🔧 TokenContext grant type: " +
        // tokenContext.getAuthorizationGrantType());

        // OAuth2AccessToken accessToken = tokenGenerator.generate(tokenContext);

        // System.out.println("🔧 Token: " + accessToken);
        // if (accessToken == null) {
        // throw new RuntimeException("Token generation failed");
        // }

        // Task: Check if the password is correct
        if (bCryptPasswordEncoder.matches(password, user.getHashedPassword())) {
            String tokenVal = null;

            // if (authentication.isAuthenticated()) {
            // tokenVal = JWTService.generateToken(email);
            // } else {
            // throw new IncorrectPassword("Incorrect password");
            // }
            Token token = new Token();
            token.setUser(user);
            token.setValue(RandomStringUtils.randomAlphanumeric(18));
            LocalDate today = LocalDate.now();
            LocalDate onedayLater = today.plusDays(1);
            Date expiryAt = Date.from(onedayLater.atStartOfDay(ZoneId.systemDefault()).toInstant());
            token.setExpiryAt(expiryAt);
            return tokenRepository.save(token);
        } else {
            // throw exception
            throw new IncorrectPassword("Incorrect password");
        }
    }

    public void logout(String token) {
        Optional<Token> token1 = tokenRepository.findByValueAndDeletedEquals(token, false);

        if (token1.isEmpty()) {
            // throw exception
        }

        Token t = token1.get();
        t.setDeleted(true);
        tokenRepository.save(t);
    }

    public User validateToken(String token) throws UnAuthorized {
        Optional<Token> token1 = tokenRepository.findByValueAndDeletedEquals(token, false);
        // Optional<Token> token1 =
        // tokenRepository.findByValueAndDeletedEqualsAndExipryAtGreaterThan(token,
        // false);

        if (token1.isEmpty()) {
            throw new UnAuthorized("Token is not valid");
        }

        Token t = token1.get();
        if (t.getExpiryAt().before(new Date())) {
            throw new UnAuthorized("Token is expired");
        }

        return t.getUser();
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }
        return user.get();
    }

    public boolean verifyUser(Long userId) {
        return userRepository.existsById(userId);
    }
}
