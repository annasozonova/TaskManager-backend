package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.QualificationService;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3030")
public class UserController {

    public final UserService userService;
    public final QualificationService qualificationService;

    @Autowired
    public UserController(UserService userService, QualificationService qualificationService) {
        this.userService = userService;
        this.qualificationService = qualificationService;
    }

    /**
     * Registers a new user.
     * @param user The user to register.
     * @return The registered user with HTTP status CREATED.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Updates an existing user.
     * @param id The ID of the user to update.
     * @param updatedUser The updated user details.
     * @return The updated user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Update the qualification of a user.
     * @param userId The id of the user whose qualification is to be updated.
     * @param qualification The new qualification information.
     * @return The updated qualification of the user.
     */
    @PutMapping("/{userId}/qualification")
    public ResponseEntity<Qualification> updateUserQualification(@PathVariable Integer userId, @RequestBody Qualification qualification) {
        // Fetch the existing user
        User user = userService.getUserById(userId);

        // Link the qualification with the user
        qualification.setUser(user);
        // Save the qualification
        Qualification updatedQualification = qualificationService.createQualification(qualification);

        // Update the user's qualification field
        user.setQualification(updatedQualification);
        // Save the user with updated qualification
        userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(updatedQualification);
    }

    /**
     * Handles validation exceptions.
     * @param ex The exception containing validation errors.
     * @return A response entity with validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getAllErrors().stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.joining(","));
        return ResponseEntity.badRequest().body("Validation failed: " + error);
    }
}