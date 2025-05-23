package com.example.taskmanager.service;

import com.example.taskmanager.entity.*;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing tasks. It provides methods for creating, updating,
 * deleting, and retrieving tasks, as well as handling task assignment and notifications.
 */
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    /**
     * Creates a new task and assigns it automatically to a user with the least tasks.
     * If the task does not have a priority, status, or qualification, defaults are assigned.
     *
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

        logger.info("Task before user appointment: {}", task.toString());

        if (task.getAssignedTo() == null) {
            taskRepository.save(task);
            logger.info("Calling assignTaskAutomatically for task {}", task.getId());
            return assignTaskAutomatically(task);
        }
        else{
            User user = userRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found when creating a task"));
            task.setAssignedTo(user);
            taskRepository.save(task);
            notificationService.sendNotification("You have been assigned a new task: " + task.getTitle(), user.getUsername(), Notification.NotificationType.TASK, task.getId());
            notificationService.sendDepartmentHeadNotification("A new task has been created: " + task.getTitle(), department.getId(), Notification.NotificationType.TASK, task.getId());
            return task;
        }
    }

    /**
     * Retrieves all tasks from the repository.
     *
     * @return A list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id The ID of the task.
     * @return The task with the given ID.
     * @throws ResourceNotFoundException if the task is not found.
     */
    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Task not found with id " + id));
    }

    /**
     * Updates an existing task with new details.
     *
     * @param id The ID of the task to update.
     * @param task The task object containing updated details.
     * @return The updated task.
     * @throws ResourceNotFoundException if the task is not found.
     */
    public Task updateTask(Integer id, Task task) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setPriority(task.getPriority());
        existingTask.setStatus(task.getStatus());
        existingTask.setAssignedTo(task.getAssignedTo());
        existingTask.setDepartment(task.getDepartment());
        existingTask.setRequiredQualification(task.getRequiredQualification());
        existingTask.setComments(task.getComments());

        Task updatedTask = taskRepository.save(existingTask);

        notificationService.sendDepartmentHeadNotification("Task updated: " + existingTask.getTitle(), existingTask.getDepartment().getId(), Notification.NotificationType.TASK, existingTask.getId());

        return updatedTask;
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id The ID of the task to delete.
     * @throws ResourceNotFoundException if the task is not found.
     */
    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Finds the user with the least number of assigned tasks.
     *
     * @param users A list of users to check.
     * @return The user with the least tasks.
     * @throws ResourceNotFoundException if no users are available.
     */
    private User getUserWithLeastTasks(List<User> users) {
        return users.stream().min(Comparator.comparingInt(u -> u.getTasks().size()))
                .orElseThrow(() -> new ResourceNotFoundException("No users available"));
    }

    /**
     * Counts the number of tasks assigned to a specific user.
     *
     * @param user The user whose tasks are counted.
     * @return The number of tasks assigned to the user.
     */
    private int getTaskCountForUser(User user) {
        return user.getTasks().size();
    }

    /**
     * Automatically assigns a task to the user with the least number of tasks.
     * Filters users by department, role, and qualification before assigning the task.
     *
     * @param task The task to assign.
     * @return The task with the assigned user.
     * @throws ResourceNotFoundException if no available users match the task requirements.
     */
    public Task assignTaskAutomatically(Task task) {
        logger.info("assignTaskAutomatically started");
        List<User> users = userRepository.findAll();
        logger.info("All users before filtering: {}", users);

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

        Task updatedTask = updateTask(task.getId(), task);
        logger.info("Assigned to user: {}", task.getAssignedTo());
        logger.info("Assigned user ID: {}", task.getAssignedTo() != null ? task.getAssignedTo().getId() : "null");

        notificationService.sendNotification("You have been assigned a new task: " + task.getTitle(), userWithLeastTasks.getUsername(), Notification.NotificationType.TASK, task.getId());

        return updatedTask;
    }

    /**
     * Retrieves tasks assigned to a specific department.
     *
     * @param id The department ID.
     * @return A list of tasks for the given department.
     */
    public List<Task> findTasksByDepartment(Integer id) {
        return taskRepository.findByDepartmentId(id);
    }

    /**
     * Retrieves tasks assigned to a specific user.
     *
     * @param id The user ID.
     * @return A list of tasks assigned to the given user.
     */
    public List<Task> findTasksByUser(Integer id) {
        return taskRepository.findByAssignedTo(userRepository.findById(id).get());
    }

    /**
     * Retrieves a task by its ID.
     *
     * @param id The ID of the task.
     * @return The task with the given ID.
     */
    public Task findTaskById(Integer id) {
        return taskRepository.findById(id).get();
    }

    /**
     * Schedules a daily task to check if any tasks are due in the next 3 days.
     * Sends reminders to the assigned user and department head.
     */
    @Scheduled(cron = "0 0 12 * * *") // Triggered every day at 12 PM
    public void checkUpcomingDueDates() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        List<Task> upcomingTasks = taskRepository.findByDueDateBetween(today, threeDaysFromNow);
        for (Task task : upcomingTasks) {
            notificationService.sendNotification("Reminder: The due date for your task '" + task.getTitle() + "' is approaching on " + task.getDueDate(), task.getAssignedTo().getUsername(), Notification.NotificationType.TASK, task.getId());
            notificationService.sendDepartmentHeadNotification("Reminder: The due date for task '" + task.getTitle() + "' is approaching on " + task.getDueDate(), task.getDepartment().getId(), Notification.NotificationType.TASK, task.getId());
        }
    }

    /**
     * Adds a comment to a task.
     *
     * @param taskId The ID of the task to add the comment to.
     * @param comment The comment to add to the task.
     * @throws RuntimeException if the task is not found.
     */
    @Transactional
    public void addComment(Integer taskId, String comment) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        if (comment != null && !comment.trim().isEmpty()) {
            TaskComment taskComment = new TaskComment(task, comment);
            taskCommentRepository.save(taskComment);
        }
    }

    /**
     * Retrieves all comments for a specific task.
     *
     * @param taskId The ID of the task.
     * @return A list of comments for the task.
     */
    public List<TaskComment> getComments(Integer taskId) {
        return taskCommentRepository.findByTaskId(taskId);
    }

    /**
     * Counts the number of tasks assigned to a specific user.
     *
     * @param userId The user ID.
     * @return The number of tasks assigned to the user.
     */
    public Integer countTasksByUser(Integer userId) {
        return taskRepository.countByAssignedTo(userId);
    }

    /**
     * Counts the number of tasks with a specific status assigned to a user.
     *
     * @param userId The user ID.
     * @param status The status of the tasks to count.
     * @return The number of tasks with the given status assigned to the user.
     */
    public Integer countTasksByUserAndStatus(Integer userId, Task.TaskStatus status) {
        return taskRepository.countByAssignedToAndStatus(userId, status);
    }

    /**
     * Counts the number of tasks in a specific department.
     *
     * @param departmentId The department ID.
     * @return The number of tasks in the department.
     */
    public Integer countTasksByDepartment(Integer departmentId) {
        return taskRepository.countByDepartment(departmentId);
    }

    /**
     * Counts the number of tasks in a specific department with a specific status.
     *
     * @param departmentId The department ID.
     * @param status The status of the tasks to count.
     * @return The number of tasks with the given status in the department.
     */
    public Integer countTasksByDepartmentAndStatus(Integer departmentId, Task.TaskStatus status) {
        return taskRepository.countByDepartmentAndStatus(departmentId, status);
    }

    /**
     * Counts the total number of tasks in the system.
     *
     * @return The total number of tasks.
     */
    public Integer countAllTasks() {
        return Math.toIntExact(taskRepository.count());
    }

    /**
     * Counts the number of tasks with a specific status across all departments.
     *
     * @param status The status of the tasks to count.
     * @return The number of tasks with the given status.
     */
    public Integer countTasksByStatus(Task.TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    /**
     * Retrieves high-priority tasks assigned to a specific user.
     *
     * @param userId The user ID.
     * @return A list of high-priority tasks assigned to the user.
     */
    public List<Task> findHighPriorityTasksByUser(Integer userId) {
        return taskRepository.findByAssignedToAndPriority(userId, Task.TaskPriority.HIGH);
    }

    /**
     * Retrieves high-priority tasks assigned to a specific department.
     *
     * @param departmentId The department ID.
     * @return A list of high-priority tasks assigned to the department.
     */
    public List<Task> findHighPriorityTasksByDepartment(Integer departmentId) {
        return taskRepository.findByDepartmentAndPriority(departmentId, Task.TaskPriority.HIGH);
    }

    /**
     * Retrieves all high-priority tasks in the system.
     *
     * @return A list of all high-priority tasks.
     */
    public List<Task> findAllHighPriorityTasks() {
        return taskRepository.findByPriority(Task.TaskPriority.HIGH);
    }

    /**
     * Retrieves overdue tasks assigned to a specific user.
     *
     * @param userId The user ID.
     * @return A list of overdue tasks assigned to the user.
     */
    public List<Task> findOverdueTasksByUser(Integer userId) {
        return taskRepository.findOverdueTasksByUser(userId, LocalDate.now());
    }

    /**
     * Retrieves overdue tasks assigned to a specific department.
     *
     * @param departmentId The department ID.
     * @return A list of overdue tasks assigned to the department.
     */
    public List<Task> findOverdueTasksByDepartment(Integer departmentId) {
        return taskRepository.findOverdueTasksByDepartment(departmentId, LocalDate.now());
    }

    /**
     * Retrieves all overdue tasks in the system.
     *
     * @return A list of all overdue tasks.
     */
    public List<Task> findAllOverdueTasks() {
        return taskRepository.findAllOverdueTasks(LocalDate.now());
    }

    /**
     * Retrieves the task load for users in a specific department.
     *
     * @param departmentId The department ID.
     * @return A list of user task load data for the department.
     */
    public List<Map<String, Object>> getUserTaskLoadByDepartment(Integer departmentId) {
        List<Object[]> results = taskRepository.getUserTaskLoadByDepartment(departmentId);
        List<Map<String, Object>> userTaskLoadList = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", row[0]);
            userData.put("username", row[1]);
            userData.put("taskCount", row[2]);
            userTaskLoadList.add(userData);
        }

        return userTaskLoadList;
    }

    /**
     * Retrieves the task load for users across all departments.
     *
     * @return A list of user task load data for all departments.
     */
    public List<Map<String, Object>> getUserTaskLoadForAllDepartments() {

        List<Object[]> results = taskRepository.getUserTaskLoadForAllDepartments();
        List<Map<String, Object>> userTaskLoadList = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", row[0]);
            userData.put("username", row[1]);
            userData.put("taskCount", row[2]);
            userTaskLoadList.add(userData);
        }

        return userTaskLoadList;
    }
}
