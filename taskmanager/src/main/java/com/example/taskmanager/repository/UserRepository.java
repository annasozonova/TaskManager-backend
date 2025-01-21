package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> getUsersByDepartment(Department department);
}

