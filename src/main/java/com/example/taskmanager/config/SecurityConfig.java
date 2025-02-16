package com.example.taskmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration class for the application.
 * Configures JWT-based authorization, CORS settings, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter; // Filter for handling JWT authentication

    /**
     * Configures the security filter chain.
     * Sets up CORS, disables CSRF protection, and configures access control for different endpoints.
     * The JwtAuthorizationFilter is added to the filter chain before the UsernamePasswordAuthenticationFilter.
     *
     * @param http HttpSecurity object for configuring HTTP security
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF (Cross-Site Request Forgery) protection
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/login", "/api/login/**").permitAll() // Allow unauthenticated access to login endpoints
                                .anyRequest().authenticated() // All other requests require authentication
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter before the authentication filter

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * Allows requests from the front-end application running on "http://localhost:3000".
     * Configures allowed methods (GET, POST, PUT, DELETE, OPTIONS) and headers.
     *
     * @return the configured CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // Allow credentials (cookies, HTTP authentication)
        configuration.addAllowedOrigin("http://localhost:3000"); // Allow requests from the front-end origin
        configuration.addAllowedHeader("*"); // Allow all headers
        configuration.addAllowedMethod("OPTIONS"); // Allow OPTIONS method for pre-flight requests
        configuration.addAllowedMethod("GET"); // Allow GET method
        configuration.addAllowedMethod("POST"); // Allow POST method
        configuration.addAllowedMethod("PUT"); // Allow PUT method
        configuration.addAllowedMethod("DELETE"); // Allow DELETE method
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS configuration to all endpoints
        return source;
    }

    /**
     * Configures a BCryptPasswordEncoder bean for password hashing.
     * BCrypt is used to securely hash passwords before storing them.
     *
     * @return the BCryptPasswordEncoder bean
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
