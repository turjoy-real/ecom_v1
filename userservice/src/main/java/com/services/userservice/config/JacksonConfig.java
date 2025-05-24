package com.services.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.services.userservice.mixins.CustomUserDetailsMixin;
import com.services.userservice.mixins.RoleMixin;
import com.services.userservice.mixins.UserMixin;
import com.services.userservice.models.Role;
import com.services.userservice.models.User;
import com.services.userservice.security.models.CustomUserDetails;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Configuration
public class JacksonConfig {
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Spring Security Jackson modules
        mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        // Add your custom mixins
        mapper.addMixIn(CustomUserDetails.class, CustomUserDetailsMixin.class);
        mapper.addMixIn(User.class, UserMixin.class);
        mapper.addMixIn(Role.class, RoleMixin.class);

        // Configure type inclusion for trusted sources
        // mapper.activateDefaultTyping(
        // mapper.getPolymorphicTypeValidator(),
        // ObjectMapper.DefaultTyping.NON_FINAL,
        // JsonTypeInfo.As.PROPERTY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        return mapper;
    }
}