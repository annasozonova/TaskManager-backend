package com.example.taskmanager.controller;

import com.example.taskmanager.entity.*;
import java.util.HashMap;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing user-related pages and interactions.
 * This controller handles endpoints for retrieving user notifications, updating user profile,
 * and displaying the user dashboard, based on the user's role.
 * It serves pages accessible to employees, department heads, and administrators.
 */
@RestController
@RequestMapping("api/user")
public class UserPagesController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves notifications for the currently authenticated employee.
     * @param userDetails The details of the currently authenticated user.
     * @return A list of unread notifications for the employee.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getEmployeeNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }
        List<Notification> notifications = notificationService.findNotificationsByUserAndReadFalse(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Updates the profile of the currently authenticated employee.
     * Allows updates to the username, password, email, and technologies.
     * @param userDetails The details of the currently authenticated user.
     * @param updatedFields The fields to be updated in the employee's profile.
     * @return The updated employee profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<User> getEmployeeProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> updatedFields) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }

        // Update fields based on the provided data
        if (updatedFields.containsKey("username")) {
            currentUser.setUsername((String) updatedFields.get("username"));
        }
        if (updatedFields.containsKey("password")) {
            currentUser.setPassword((String) updatedFields.get("password"));
        }
        if (updatedFields.containsKey("email")) {
            currentUser.setEmail((String) updatedFields.get("email"));
        }
        if (updatedFields.containsKey("technologies")) {
            Qualification currentQualification = currentUser.getQualification();
            currentQualification.setTechnologies((String) updatedFields.get("technologies"));
        }

        User savedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Retrieves the profile of the currently authenticated employee.
     * @param userDetails The details of the currently authenticated user.
     * @return The current employee profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getEmployeeProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(currentUser);
    }

    /**
     * Retrieves dashboard data for the currently authenticated employee, department head, or admin.
     * Includes task counts, high-priority tasks, overdue tasks, and additional data depending on the user role.
     * @param userDetails The details of the currently authenticated user.
     * @return A map containing the dashboard data.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return ResponseEntity.status(404).body(null);
        }

        Map<String, Object> dashboardData = new HashMap<>();
        List<Task> highPriorityTasks;
        List<Task> overdueTasks;

        // User-related data
        dashboardData.put("user", Map.of(
                "id", currentUser.getId(),
                "role", currentUser.getRole().toString()
        ));

        // Data for employees
        if (currentUser.getRole() == User.UserRole.EMPLOYEE) {
            dashboardData.put("taskCount", taskService.countTasksByUser(currentUser.getId()));
            dashboardData.put("pendingCount", taskService.countTasksByUserAndStatus(currentUser.getId(), Task.TaskStatus.PENDING));
            dashboardData.put("inProgressCount", taskService.countTasksByUserAndStatus(currentUser.getId(), Task.TaskStatus.IN_PROGRESS));
            dashboardData.put("completedCount", taskService.countTasksByUserAndStatus(currentUser.getId(), Task.TaskStatus.COMPLETED));

            highPriorityTasks = taskService.findHighPriorityTasksByUser(currentUser.getId());
            overdueTasks = taskService.findOverdueTasksByUser(currentUser.getId());
        }
        // Data for department heads
        else if (currentUser.getRole() == User.UserRole.DEPARTMENT_HEAD) {
            Department department = currentUser.getDepartment();
            dashboardData.put("taskCount", taskService.countTasksByDepartment(department.getId()));
            dashboardData.put("pendingCount", taskService.countTasksByDepartmentAndStatus(department.getId(), Task.TaskStatus.PENDING));
            dashboardData.put("inProgressCount", taskService.countTasksByDepartmentAndStatus(department.getId(), Task.TaskStatus.IN_PROGRESS));
            dashboardData.put("completedCount", taskService.countTasksByDepartmentAndStatus(department.getId(), Task.TaskStatus.COMPLETED));
            dashboardData.put("activeUserCount", userService.countActiveUsersByDepartment(department.getId()));

            highPriorityTasks = taskService.findHighPriorityTasksByDepartment(department.getId());
            overdueTasks = taskService.findOverdueTasksByDepartment(department.getId());

            List<Map<String, Object>> userTaskLoads = taskService.getUserTaskLoadByDepartment(department.getId());
            dashboardData.put("userTaskLoads", userTaskLoads);
        }
        // Data for admins
        else if (currentUser.getRole() == User.UserRole.ADMIN) {
            dashboardData.put("userCount", userService.countAllUsers());
            dashboardData.put("taskCount", taskService.countAllTasks());
            dashboardData.put("pendingCount", taskService.countTasksByStatus(Task.TaskStatus.PENDING));
            dashboardData.put("inProgressCount", taskService.countTasksByStatus(Task.TaskStatus.IN_PROGRESS));
            dashboardData.put("completedCount", taskService.countTasksByStatus(Task.TaskStatus.COMPLETED));

            highPriorityTasks = taskService.findAllHighPriorityTasks();
            overdueTasks = taskService.findAllOverdueTasks();

            dashboardData.put("inactiveUsers", userService.findInactiveUsers());

            List<Map<String, Object>> userTaskLoads = taskService.getUserTaskLoadForAllDepartments();
            dashboardData.put("userTaskLoads", userTaskLoads);
        } else {
            return ResponseEntity.status(403).body(null);
        }

        dashboardData.put("highPriorityTasks", highPriorityTasks);
        dashboardData.put("overdueTasks", overdueTasks);

        return ResponseEntity.ok(dashboardData);
    }
}
