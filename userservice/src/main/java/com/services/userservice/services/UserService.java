package com.services.userservice.services;

import com.services.userservice.models.Token;
import com.services.userservice.models.User;
import com.services.userservice.repositories.TokenRepo;
import com.services.userservice.repositories.UserRepo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

// Ideally should be an interface
@Service // This is a bean, so we can inject it anywhere we need it.
public class UserService {
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepo userRepository;
    private TokenRepo tokenRepository;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserRepo userRepository,
                       TokenRepo tokenRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }
    public User signUp(String name, String email, String password) {
        //Validation

        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setHashedPassword(bCryptPasswordEncoder.encode(password));

        User user = userRepository.save(u);
        return user;
    }

    public Token login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // THROW EXCEPTION
            return null;
        }

        User user = userOptional.get();
        if (bCryptPasswordEncoder.matches(password, user.getHashedPassword())) {
            Token token = new Token();
            token.setUser(user);
            token.setValue(RandomStringUtils.randomAlphabetic(128));
            LocalDate today = LocalDate.now();
            LocalDate onedayLater = today.plusDays(1);
            Date expiryAt = Date.from(onedayLater.atStartOfDay(ZoneId.systemDefault()).toInstant());
            token.setExpiryAt(expiryAt);
            return tokenRepository.save(token);
        }
        return null;
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

    public User validateToken(String token) {
        Optional<Token> token1 = tokenRepository.findByValueAndDeletedEquals(token, false);
        //Optional<Token> token1 = tokenRepository.findByValueAndDeletedEqualsAndExipryAtGreaterThan(token, false);

        if (token1.isEmpty()) {
            return null;
        }

        Token t = token1.get();
        if (t.getExpiryAt().before(new Date())) {
            return null;
        }

        return t.getUser();
    }
}
