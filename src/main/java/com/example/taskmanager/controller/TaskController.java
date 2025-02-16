package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Notification;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskComment;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing tasks in the system.
 * This controller provides endpoints to create, update, retrieve, delete tasks,
 * as well as to add and retrieve comments for tasks. It also handles task status updates and sends notifications.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private final TaskService taskService;

    @Autowired
    private final NotificationService notificationService;

    @Autowired
    private UserService userService;

    public TaskController(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new task.
     * @param task The task to create.
     * @return The created task with HTTP status CREATED.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Retrieves all tasks assigned to the currently authenticated user.
     * The tasks returned depend on the role of the user:
     * - Admin: Retrieves all tasks.
     * - Department head: Retrieves tasks for the specific department.
     * - Employee: Retrieves tasks assigned to the user.
     * @param userDetails The authenticated user's details.
     * @return A list of tasks based on the user's role.
     */
    @GetMapping
    public ResponseEntity<?> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User currentUser = userService.findByUsername(userDetails.getUsername());
            if (currentUser == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            List<Task> tasks;
            if (currentUser.getRole() == User.UserRole.ADMIN){
                tasks = taskService.getAllTasks();
            }
            else if (currentUser.getRole() == User.UserRole.DEPARTMENT_HEAD) {
                tasks = taskService.findTasksByDepartment(currentUser.getDepartment().getId());
            } else {
                tasks = taskService.findTasksByUser(currentUser.getId());
            }

            return ResponseEntity.ok(tasks);
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }

    /**
     * Retrieves a task by its ID.
     * @param id The ID of the task.
     * @return The task with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Updates a task's status, description, or adds a comment.
     * The description can only be updated by Admin or Department Head.
     * @param id The ID of the task to update.
     * @param updates A map containing the fields to update (status, description, comments).
     * @param userDetails The authenticated user's details.
     * @return The updated task.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody Map<String, Object> updates, @AuthenticationPrincipal UserDetails userDetails) {
        Task task = taskService.findTaskById(id);

        if (task == null) {
            return ResponseEntity.status(404).body(null);
        }

        if (updates.containsKey("status")) {
            task.setStatus(Task.TaskStatus.valueOf((String) updates.get("status")));
        }

        if (updates.containsKey("comments")) {
            taskService.addComment(task.getId(), (String) updates.get("comments"));
            notificationService.sendNotification("New comment on task: " + task.getTitle() + " - " + updates.get("comments"), task.getAssignedTo().getUsername(), Notification.NotificationType.TASK, task.getId());
        }

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if ("ROLE_ADMIN".equals(role) || "ROLE_DEPARTMENT_HEAD".equals(role)) {
            if (updates.containsKey("description")) {
                task.setDescription((String) updates.get("description"));
            }
        }

        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Deletes a task by its ID.
     * @param id The ID of the task to delete.
     * @return HTTP status NO CONTENT indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a comment to a task and sends a notification to the assigned user.
     * @param taskId The ID of the task to add a comment to.
     * @param comment The comment text.
     * @return A response indicating success.
     */
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Integer taskId, @RequestBody String comment) {
        Task task = taskService.findTaskById(taskId);
        taskService.addComment(taskId, comment);
        notificationService.sendNotification("New comment on task: " + task.getTitle() + " - " + comment, task.getAssignedTo().getUsername(), Notification.NotificationType.TASK, taskId);
        return ResponseEntity.ok("Comment added successfully");
    }

    /**
     * Retrieves all comments for a specific task.
     * @param taskId The ID of the task.
     * @return A list of comments for the task.
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskComment>> getComments(@PathVariable Integer taskId) {
        List<TaskComment> comments = taskService.getComments(taskId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Handles validation exceptions and returns error messages.
     * @param ex The exception containing validation errors.
     * @return A response entity with the validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
    }
}
