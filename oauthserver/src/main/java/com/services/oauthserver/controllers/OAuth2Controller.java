package com.services.oauthserver.controllers;

import com.nimbusds.jose.JOSEException;
import com.services.oauthserver.models.User;
import com.services.oauthserver.repositories.UserRepo;
import com.services.oauthserver.security.models.CustomUserDetails;
import com.services.oauthserver.services.OAuth2TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
public class OAuth2Controller {

    private final UserRepo userRepo;
    private final OAuth2TokenService oauth2TokenService;

    public OAuth2Controller(UserRepo userRepo, OAuth2TokenService oauth2TokenService) {
        this.userRepo = userRepo;
        this.oauth2TokenService = oauth2TokenService;
    }

    @GetMapping("/oauth2/success")
    public RedirectView oauth2Success(@RequestParam(required = false) String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email = null;
            
            if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                email = oauth2User.getAttribute("email");
            } else if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                email = userDetails.getUsername();
            }
            
            if (email != null) {
                Optional<User> userOpt = userRepo.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    try {
                        String accessToken = oauth2TokenService.generateAccessToken(user);
                        
                        // Redirect to frontend with the OAuth2 access token
                        return new RedirectView("/?token=" + accessToken + "&login=success");
                    } catch (JOSEException e) {
                        // If token generation fails, redirect to error page
                        return new RedirectView("/?error=token_generation_failed");
                    }
                }
            }
        }
        
        // If something went wrong, redirect to login page
        return new RedirectView("/?error=oauth2_failed");
    }

    @GetMapping("/oauth2/failure")
    public RedirectView oauth2Failure(@RequestParam String error, 
                                     @RequestParam(required = false) String error_description) {
        String errorMsg = "oauth2_error=" + error;
        if (error_description != null) {
            errorMsg += "&error_description=" + error_description;
        }
        return new RedirectView("/?" + errorMsg);
    }
} 