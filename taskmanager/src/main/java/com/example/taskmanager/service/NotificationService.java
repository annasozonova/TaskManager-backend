package com.example.taskmanager.service;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> findNotificationsByUser(Integer userId){
        return notificationRepository.findByUserId(userId);
    }
}
