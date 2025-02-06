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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Retrieves all tasks.
     * @return A list of all tasks.
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

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody Map<String, Object> updates, @AuthenticationPrincipal UserDetails userDetails) {
        Task task = taskService.findTaskById(id);

        if (task == null) {
            return ResponseEntity.status(404).body(null);
        }

        // Обновление статуса задачи
        if (updates.containsKey("status")) {
            task.setStatus(Task.TaskStatus.valueOf((String) updates.get("status")));
        }

        // Добавление комментария к задаче
        if (updates.containsKey("comments")) {
            taskService.addComment(task.getId(), (String) updates.get("comments"));
            notificationService.sendNotification("New comment on task: " + task.getTitle() + " - " + updates.get("comments"), task.getAssignedTo().getUsername(), Notification.NotificationType.TASK, task.getId());
        }

        // Обновление описания задачи (только если выдавший задачу)
        if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_DEPARTMENT_HEAD"))) {
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
     * @return HTTP status NO CONTENT.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Integer taskId, @RequestBody String comment) {
        Task task = taskService.findTaskById(taskId);
        taskService.addComment(taskId, comment);
        notificationService.sendNotification("New comment on task: " + task.getTitle() + " - " + comment, task.getAssignedTo().getUsername(), Notification.NotificationType.TASK, taskId);
        return ResponseEntity.ok("Comment added successfully");
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskComment>> getComments(@PathVariable Integer taskId) {
        List<TaskComment> comments = taskService.getComments(taskId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Handles validation exceptions.
     * @param ex The exception containing validation errors.
     * @return A response entity with validation error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
    }
}
