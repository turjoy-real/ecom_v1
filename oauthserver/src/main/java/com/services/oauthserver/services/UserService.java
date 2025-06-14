package com.services.oauthserver.services;

import com.services.oauthserver.exceptions.TokenNotFound;
import com.services.oauthserver.exceptions.UserAlreadyRegistered;

import com.services.oauthserver.models.Role;
import com.services.oauthserver.models.Token;
import com.services.oauthserver.models.User;
import com.services.oauthserver.repositories.RoleRepository;
import com.services.oauthserver.repositories.TokenRepo;
import com.services.oauthserver.repositories.UserRepo;

import lombok.AllArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor // This is a bean, so we can inject it anywhere we need it.
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepo userRepository;
    private final TokenRepo tokenRepository;
    private final RoleRepository roleRepository;
    private final OAuth2AuthorizationService oauth2AuthorizationService;

    public User signUp(String name, String email, String password) throws UserAlreadyRegistered {
        // Validation
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyRegistered("User is already registered");
        }
        // Fetch default role, or create it if not found
        Role role = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    return roleRepository.save(newRole);
                });

        // Create user
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));
        user.setRoles(List.of(role)); // ✅ Set roles directly

        // Save
        user = userRepository.save(user);

        return user;
    }

    public void logout(String token) {
        // First try to find and invalidate regular token
        Optional<Token> regularToken = tokenRepository.findByValueAndDeletedEquals(token, false);
        if (regularToken.isPresent()) {
            Token t = regularToken.get();
            t.setDeleted(true);
            tokenRepository.save(t);
            return;
        }

        // If not found as regular token, try to find and remove OAuth2 authorization
        OAuth2Authorization oauth2Auth = oauth2AuthorizationService.findByToken(token, null);
        if (oauth2Auth != null) {
            oauth2AuthorizationService.remove(oauth2Auth);
            return;
        }

        throw new TokenNotFound("Token not found or already deleted");
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
