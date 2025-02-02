package com.example.taskmanager.service;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.DepartmentRepository;
import com.example.taskmanager.repository.QualificationRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);


    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new task and assigns it automatically to a user with the least tasks.
     * @param task The task to create.
     * @return The created task with assigned user.
     */
    public Task createTask(Task task) {
        if (task.getPriority().toString().trim().isEmpty() || task.getPriority() == null) {
            task.setPriority(Task.TaskPriority.MEDIUM);
        }
        if (task.getStatus() == null) {
            task.setStatus(Task.TaskStatus.PENDING);
        }
        if (task.getRequiredQualification() == null) {
            task.setRequiredQualification(Qualification.QualificationType.JUNIOR);
        }

        logger.info("Task received: {}", task.toString());
        Department department = departmentRepository.findById(task.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found when creating a task"));
        task.setDepartment(department);

        logger.info("Saved task: {}", task.toString());
        if (task.getAssignedTo() == null) {
            taskRepository.save(task);
            return assignTaskAutomatically(task);
        }
        else{
            User user = userRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found when creating a task"));
            task.setAssignedTo(user);
            return taskRepository.save(task);
        }
    }

    /**
     * Retrieves all tasks.
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Retrieves a task by its ID.
     * @param id The ID of the task.
     * @return The task with the given ID.
     */
    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Task not found with id " + id));
    }

    /**
     * Updates an existing task.
     * @param id The ID of the task to update.
     * @param task The task object containing updated details.
     * @return The updated task.
     */
    public Task updateTask(Integer id, Task task) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        task.setId(id);
        return taskRepository.save(task);
    }

    /**
     * Deletes a task by its ID.
     * @param id The ID of the task to delete.
     */
    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Finds the user with the least number of assigned tasks.
     * @param users A list of users to check.
     * @return The user with the least tasks.
     */
    private User getUserWithLeastTasks(List<User> users){
        return users.stream().min(Comparator.comparingInt(this::getTaskCountForUser))
                .orElseThrow(() -> new RuntimeException("No users available for task assignment"));
    }

    /**
     * Counts the number of tasks assigned to a specific user.
     * @param user The user whose tasks are counted.
     * @return The number of tasks assigned to the user.
     */
    private int getTaskCountForUser(User user) {
        return user.getTasks().size();
    }

    /**
     * Automatically assigns a task to the user with the least number of tasks.
     * @param task The task to assign.
     * @return The task with the assigned user.
     */
    public Task assignTaskAutomatically(Task task) {
        List<User> users = userRepository.findAll();
        logger.info("All users before filtering: {}", users);

        // Фильтрация пользователей по роли
        users = users.stream()
                .filter(user -> {
                    boolean isEmployee = user.getRole() == User.UserRole.EMPLOYEE;
                    logger.info("User {} is an employee: {}", user.getId(), isEmployee);
                    return isEmployee;
                })
                .filter(user -> {
                    boolean isCorrectDepartment = user.getDepartment() != null &&
                            user.getDepartment().getId() == task.getDepartment().getId();
                    logger.info("User {} is in department {}: {}", user.getId(),
                            task.getDepartment().getId(), isCorrectDepartment);
                    return isCorrectDepartment;
                })
                .filter(user -> {
                    boolean isCorrectQualification = user.getQualification() != null &&
                            user.getQualification().getQualification() == task.getRequiredQualification();
                    logger.info("User {} has qualification {}: {}", user.getId(),
                            user.getQualification() != null ? user.getQualification().getQualification() : "null",
                            isCorrectQualification);
                    return isCorrectQualification;
                })
                .collect(Collectors.toList());

        logger.info("Filtered users: {}", users);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No available users matching the task requirements");
        }

        // Assign task to the user with the least tasks
        User userWithLeastTasks = getUserWithLeastTasks(users);
        task.setAssignedTo(userWithLeastTasks);

        return updateTask(task.getId(), task);
    }


    public List<Task> findTasksByDepartment (Integer id) {
        return taskRepository.findByDepartmentId(id);
    }

    public List<Task> findTasksByUser (Integer id) {
        return taskRepository.findByAssignedTo(userRepository.findById(id).get());
    }

    public Task findTaskById(Integer id) {
        return taskRepository.findById(id).get();
    }
}
