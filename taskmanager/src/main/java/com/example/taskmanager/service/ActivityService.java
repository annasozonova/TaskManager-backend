package com.example.taskmanager.service;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ActivityService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkUserActivity() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<User> inactiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() == null || user.getLastLogin().isBefore(oneWeekAgo)).toList();
        for (User user : inactiveUsers) {
            notificationService.sendAdminNotification("User " + user.getUsername() + " has been inactive for over a week", Notification.NotificationType.OTHER, null);
        }
    }
}
