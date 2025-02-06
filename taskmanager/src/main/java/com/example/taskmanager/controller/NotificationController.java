package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Integer id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestParam String message,
                                                 @RequestParam String recipient,
                                                 @RequestParam @JsonProperty("type") Notification.NotificationType type,
                                                 @RequestParam Integer referenceId) {
        notificationService.sendNotification(message, recipient, type, referenceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        List<Notification> notifications = notificationService.findNotificationsByUsername(username);

        notifications.forEach(n -> {
            try {
                logger.info("Serialized notification: {}", n);
            } catch (Exception e) {
                logger.error("Error serializing notification", e);
            }
        });

        return ResponseEntity.ok(notifications);
    }

}
