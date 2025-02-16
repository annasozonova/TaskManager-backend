package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/**
 * Configuration class for application-wide beans.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Provides an {@link AuthenticationManager} bean for handling authentication processes.
     *
     * @param authenticationConfiguration the authentication configuration provided by Spring Security
     * @return an instance of {@link AuthenticationManager}
     * @throws Exception if an error occurs while retrieving the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
