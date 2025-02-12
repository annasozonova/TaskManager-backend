package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.DepartmentService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling requests related to departments.
 * Provides CRUD operations for departments and allows retrieval of users and tasks associated with a department.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    @Autowired
    public DepartmentController(DepartmentService departmentService, UserService userService) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

    /**
     * Creates a new department.
     * @param department The department to be created.
     * @return The created department with HTTP status CREATED (201).
     */
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return new ResponseEntity<>(createdDepartment, HttpStatus.CREATED);
    }

    /**
     * Updates an existing department by its ID.
     * @param id The ID of the department to be updated.
     * @param department The updated department details.
     * @return The updated department with HTTP status OK (200).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Integer id, @RequestBody Department department) {
        Department updatedDepartment = departmentService.updateDepartment(id, department);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Retrieves a list of all departments.
     * @return A list of all departments with HTTP status OK (200).
     */
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * Retrieves a list of all users belonging to a specific department.
     * @param departmentId The ID of the department whose users are to be retrieved.
     * @return A list of users in the specified department.
     */
    @GetMapping("/{departmentId}/users")
    public List<User> getUsersByDepartment(@PathVariable int departmentId) {
        return departmentService.getUsersByDepartmentId(departmentId);
    }

    /**
     * Retrieves a list of all tasks assigned to a specific department.
     * @param departmentId The ID of the department whose tasks are to be retrieved.
     * @return A list of tasks for the specified department.
     */
    @GetMapping("/{departmentId}/tasks")
    public List<Task> getTasksByDepartment(@PathVariable int departmentId) {
        return departmentService.getTasksByDepartmentId(departmentId);
    }
}
