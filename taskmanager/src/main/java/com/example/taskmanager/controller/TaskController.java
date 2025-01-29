package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private final TaskService taskService;

    @Autowired
    private UserService userService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
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

    /**
     * Updates an existing task by its ID.
     * @param id The ID of the task to update.
     * @param task The updated task details.
     * @return The updated task if found, or HTTP status NOT FOUND if not.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @Valid @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return updatedTask != null ? ResponseEntity.ok(updatedTask) : ResponseEntity.notFound().build();
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

    /**
     * Handles validation exceptions.
     * @param ex The exception containing validation errors.
     * @return A response entity with validation error messages.
     */
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(objectError -> objectError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
    }
}
