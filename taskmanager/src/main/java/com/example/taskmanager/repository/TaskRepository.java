package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignedTo(User user);

    List<Task> findByDepartmentId(Integer id);

    List<Task> findByDueDateBetween(LocalDate today, LocalDate threeDaysFromNow);
}

