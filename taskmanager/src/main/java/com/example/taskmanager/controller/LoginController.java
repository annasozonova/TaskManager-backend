package com.example.taskmanager.controller;

import com.example.taskmanager.config.AuthResponse;
import com.example.taskmanager.config.JwtUtil;
import com.example.taskmanager.entity.LoginRequest;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user authentication.
 * Provides endpoints for logging in and handling preflight requests.
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * Logs in a user by authenticating the provided credentials and returning a JWT token.
     *
     * @param loginRequest Contains the username and password for login.
     * @return ResponseEntity containing the JWT token if authentication is successful,
     *         or an error message if authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());

            String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole().toString());

            userService.updateUserLastLogin(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    /**
     * Handles preflight OPTIONS requests for the /login endpoint, used in CORS handling.
     *
     * @return ResponseEntity with status 200 OK for preflight requests.
     */
    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> preflightLogin() {
        return ResponseEntity.ok().build();
    }
}