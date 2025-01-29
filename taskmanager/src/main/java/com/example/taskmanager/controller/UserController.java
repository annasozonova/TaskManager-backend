package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.DepartmentService;
import com.example.taskmanager.service.QualificationService;
import com.example.taskmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());
    public final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, Object> userData) {
        try {
            User createdUser = userService.createUser(userData);
            logger.info("Created user: {}", createdUser.toString());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> userData) {
        try {
            User updatedUser = userService.updateUser(id, userData);
            logger.info("Updated user: {}", updatedUser.toString());
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error updating user with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User currentUser = userService.findByUsername(userDetails.getUsername());
            List<User> users;

            if (currentUser.getRole() == User.UserRole.DEPARTMENT_HEAD) {
                users = userService.findUsersByDepartment(currentUser.getDepartment().getId());
            } else if (currentUser.getRole() == User.UserRole.ADMIN) {
                users = userService.getAllUsers();
            } else {
                return ResponseEntity.status(403).body("Access Denied");
            }

            return ResponseEntity.ok(users);
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            logger.info("Deleted user with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getAllErrors().stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.joining(","));
        return ResponseEntity.badRequest().body("Validation failed: " + error);
    }
}
