package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/employee")
public class EmployeeController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getEmployeeNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        List<Notification> notifications = notificationService.findNotificationsByUserAndReadFalse(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> getEmployeeProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> updatedFields) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }

        // Обновление информации текущего пользователя на основе полученных полей
        if (updatedFields.containsKey("username")) {
            currentUser.setUsername((String) updatedFields.get("username"));
        }
        if (updatedFields.containsKey("password")) {
            currentUser.setPassword((String) updatedFields.get("password"));
        }
        if (updatedFields.containsKey("email")) {
            currentUser.setEmail((String) updatedFields.get("email"));
        }
        if (updatedFields.containsKey("technologies")) {
            Qualification currentQualification = currentUser.getQualification();
            currentQualification.setTechnologies((String) updatedFields.get("technologies"));
        }

        User savedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getEmployeeProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(currentUser);
    }
}
