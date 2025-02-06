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

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void sendNotification(String message, String recipient, Notification.NotificationType type, Integer referenceId) {
        User user = userRepository.findByUsername(recipient);
        Notification notification = new Notification(user, message, false, LocalDateTime.now(), type, referenceId);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(String recipient) {
        User user = userRepository.findByUsername(recipient);
        return notificationRepository.findByUserAndReadFalse(user);
    }

    public void markNotificationAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void sendDepartmentHeadNotification(String message, Integer departmentId, Notification.NotificationType type, Integer referenceId) {
        List<User> departmentHeads = userRepository.findAllByRoleAndDepartmentId(User.UserRole.DEPARTMENT_HEAD, departmentId);
        for (User departmentHead : departmentHeads) {
            sendNotification(message, departmentHead.getUsername(), type, referenceId);
        }
    }

    public void sendAdminNotification(String message, Notification.NotificationType type, Integer referenceId) {
        List<User> admins = userRepository.findAllByRole(User.UserRole.ADMIN);
        for (User admin : admins) {
            sendNotification(message, admin.getUsername(), type, referenceId);
        }
    }

    public List<Notification> findNotificationsByUserAndReadFalse(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return notificationRepository.findByUserAndReadFalse(user);
    }

    public List<Notification> findNotificationsByUsername (String username) {
        User user = userRepository.findByUsername(username);
        return notificationRepository.findByUser(user);
    }
}
