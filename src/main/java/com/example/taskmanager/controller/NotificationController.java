package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing notifications for users.
 * This controller handles endpoints for fetching unread notifications,
 * marking notifications as read, and sending notifications to users.
 * It also provides functionality to retrieve all notifications for the authenticated user.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all unread notifications for the currently authenticated user.
     * @return A list of unread notifications.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks a notification as read.
     * @param id The ID of the notification to mark as read.
     * @return A response indicating the operation was successful.
     */
    @PutMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Integer id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Sends a notification to a user.
     * @param message The content of the notification.
     * @param recipient The username of the notification recipient.
     * @param type The type of the notification.
     * @param referenceId The ID of the related reference (e.g., task or event).
     * @return A response indicating the notification was successfully sent.
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestParam String message,
                                                 @RequestParam String recipient,
                                                 @RequestParam @JsonProperty("type") Notification.NotificationType type,
                                                 @RequestParam Integer referenceId) {
        notificationService.sendNotification(message, recipient, type, referenceId);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves all notifications for the currently authenticated user.
     * @return A list of all notifications for the user.
     */
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
