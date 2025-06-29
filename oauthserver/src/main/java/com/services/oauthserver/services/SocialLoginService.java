package com.services.oauthserver.services;

import com.services.oauthserver.models.Role;
import com.services.oauthserver.models.User;
import com.services.oauthserver.repositories.RoleRepository;
import com.services.oauthserver.repositories.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SocialLoginService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(SocialLoginService.class);
    private final UserRepo userRepo;
    private final RoleRepository roleRepository;

    public SocialLoginService(UserRepo userRepo, RoleRepository roleRepository) {
        this.userRepo = userRepo;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            logger.debug("Processing OAuth2 user from provider: {}", userRequest.getClientRegistration().getRegistrationId());
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getAttribute("sub"); // Google uses "sub" as unique identifier
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        if (email == null) {
            logger.error("Email not found in OAuth2 user attributes from provider: {}", provider);
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        logger.debug("Processing OAuth2 user with email: {} from provider: {}", email, provider);

        Optional<User> userOptional = userRepo.findByEmail(email);
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user with latest info from OAuth2 provider
            user.setName(name);
            user.setEmailVerified(true); // OAuth2 users are considered verified
            user = userRepo.save(user);
            logger.info("Updated existing user via OAuth2: {}", email);
        } else {
            // Create new user
            user = registerNewUser(provider, providerId, email, name);
            logger.info("Created new user via OAuth2: {}", email);
        }

        // Create OAuth2User with custom authorities
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "email" // Use email as the name attribute key
        );
    }

    private User registerNewUser(String provider, String providerId, String email, String name) {
        // Get or create default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(""); // OAuth2 users don't need password
        user.setEmailVerified(true); // OAuth2 users are considered verified
        user.setRoles(List.of(userRole));

        return userRepo.save(user);
    }
} 