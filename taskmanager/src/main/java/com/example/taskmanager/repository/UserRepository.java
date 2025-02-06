package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByDepartmentId(Integer departmentId);

    User findByUsername(String username);

    List<User> getUsersByDepartment(Department department);

    List<User> findAllByRoleAndDepartmentId(User.UserRole userRole, Integer departmentId);

    List<User> findAllByRole(User.UserRole userRole);
}

