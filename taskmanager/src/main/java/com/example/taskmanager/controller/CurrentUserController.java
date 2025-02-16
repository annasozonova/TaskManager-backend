package com.example.taskmanager.controller;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling requests related to the current authenticated user.
 * Provides an endpoint to retrieve information about the currently authenticated user.
 */
@RestController
@RequestMapping("/api/currentUser")
public class CurrentUserController {

    @Autowired
    private UserService userService; // Service to interact with the User entity

    /**
     * Endpoint to retrieve the current authenticated user.
     * Uses the @AuthenticationPrincipal annotation to get the authenticated user's details.
     *
     * @param userDetails the details of the currently authenticated user
     * @return a ResponseEntity containing the current user's data, or an error message if the user is not found
     */
    @GetMapping("")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername()); // Retrieve the user by username
        if (currentUser == null) {
            return ResponseEntity.status(404).body("User not found"); // Return error if user not found
        }
        return ResponseEntity.ok(currentUser); // Return the current user's data
    }
}
