package com.example.taskmanager.service;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Service for monitoring user activity and notifying about inactive users.
 * This service checks for users who have been inactive for more than a week
 * and sends notifications to the admin.
 */
@Service
public class ActivityService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ActivityService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Scheduled task that checks user activity every day at midnight.
     * Finds users who have been inactive for more than a week and sends
     * a notification to the admin.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkUserActivity() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<User> inactiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() == null || user.getLastLogin().isBefore(oneWeekAgo))
                .toList();
        for (User user : inactiveUsers) {
            notificationService.sendAdminNotification("User " + user.getUsername() + " has been inactive for over a week", Notification.NotificationType.OTHER, null);
        }
    }
}
