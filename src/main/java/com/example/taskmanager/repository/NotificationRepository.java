package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserAndReadFalse(User user);

    List<Notification> findByUser(User user);
}
