package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Department findByName(String name);
}
