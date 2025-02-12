package com.example.taskmanager.service;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.NotificationRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class that handles business logic related to notifications.
 * It provides methods for sending notifications, retrieving unread notifications,
 * marking notifications as read, and sending notifications to specific users or roles.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Sends a notification to a specific user.
     * This method creates and saves a new notification for the recipient user.
     *
     * @param message The message to be sent in the notification.
     * @param recipient The username of the recipient.
     * @param type The type of notification.
     * @param referenceId The ID of the related entity (can be null).
     */
    public void sendNotification(String message, String recipient, Notification.NotificationType type, Integer referenceId) {
        User user = userRepository.findByUsername(recipient);
        Notification notification = new Notification(user, message, false, LocalDateTime.now(), type, referenceId);
        notificationRepository.save(notification);
    }

    /**
     * Retrieves all unread notifications for a specific user.
     * This method returns a list of notifications where 'read' status is false.
     *
     * @param recipient The username of the recipient user.
     * @return A list of unread notifications for the specified user.
     */
    public List<Notification> getUnreadNotifications(String recipient) {
        User user = userRepository.findByUsername(recipient);
        return notificationRepository.findByUserAndReadFalse(user);
    }

    /**
     * Marks a notification as read.
     * This method updates the 'read' status of a notification to true.
     *
     * @param id The ID of the notification to mark as read.
     * @throws RuntimeException If the notification is not found.
     */
    public void markNotificationAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Sends a notification to all department heads of a specific department.
     * This method finds all department heads and sends them the notification.
     *
     * @param message The message to be sent in the notification.
     * @param departmentId The ID of the department to target.
     * @param type The type of notification.
     * @param referenceId The ID of the related entity (can be null).
     */
    public void sendDepartmentHeadNotification(String message, Integer departmentId, Notification.NotificationType type, Integer referenceId) {
        List<User> departmentHeads = userRepository.findAllByRoleAndDepartmentId(User.UserRole.DEPARTMENT_HEAD, departmentId);
        for (User departmentHead : departmentHeads) {
            sendNotification(message, departmentHead.getUsername(), type, referenceId);
        }
    }

    /**
     * Sends a notification to all admin users.
     * This method finds all users with the 'ADMIN' role and sends them the notification.
     *
     * @param message The message to be sent in the notification.
     * @param type The type of notification.
     * @param referenceId The ID of the related entity (can be null).
     */
    public void sendAdminNotification(String message, Notification.NotificationType type, Integer referenceId) {
        List<User> admins = userRepository.findAllByRole(User.UserRole.ADMIN);
        for (User admin : admins) {
            sendNotification(message, admin.getUsername(), type, referenceId);
        }
    }

    /**
     * Retrieves unread notifications for a specific user by their user ID.
     * This method finds the user by their ID and returns a list of unread notifications.
     *
     * @param userId The ID of the user.
     * @return A list of unread notifications for the specified user.
     * @throws ResourceNotFoundException If the user is not found.
     */
    public List<Notification> findNotificationsByUserAndReadFalse(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return notificationRepository.findByUserAndReadFalse(user);
    }

    /**
     * Retrieves all notifications for a specific user by their username.
     * This method finds the user by their username and returns all notifications associated with that user.
     *
     * @param username The username of the user.
     * @return A list of notifications for the specified user.
     */
    public List<Notification> findNotificationsByUsername (String username) {
        User user = userRepository.findByUsername(username);
        return notificationRepository.findByUser(user);
    }
}