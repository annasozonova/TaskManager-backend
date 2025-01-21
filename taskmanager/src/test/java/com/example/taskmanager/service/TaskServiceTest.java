package com.example.taskmanager.service;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    private User createUser(int id, User.UserRole role, Qualification.QualificationType qualificationType, Department department, int taskCount) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        Qualification qualification = new Qualification();
        qualification.setQualification(qualificationType);
        user.setQualification(qualification);
        user.setDepartment(department);
        user.setTasks(Collections.nCopies(taskCount, new Task())); // Создание списка задач
        return user;
    }

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_throwsExceptionWhenNoUsersMatchRequirements(){
        //Arrange
        Task task = new Task();
        task.setRequiredQualification(Qualification.QualificationType.JUNIOR);

        when(userRepository.findAll()).thenReturn(Arrays.asList());

        //Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->{
            taskService.createTask(task);
        });

        assertEquals("No available users matching the task requirements", exception.getMessage());
    }

    @Test
    void getTaskById_returnsTaskWhenExists() {
        //Arrange
        Task task = new Task();
        task.setId(1);

        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        //Act
        Task foundTask = taskService.getTaskById(1);

        //Assert
        assertNotNull(foundTask);
        assertEquals(task, foundTask);
        verify(taskRepository, times(1)).findById(1);
    }

    @Test
    void getTaskById_throwsExceptionWhenNotFound() {
        //Arrange
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        //Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->{
            taskService.getTaskById(1);
        });

        assertEquals("Task not found with id 1", exception.getMessage());
    }

    @Test
    void deleteTask_throwsExceptionWhenTaskDoesNotExist() {
        //Arrange
        when(taskRepository.existsById(1)).thenReturn(false);

        //Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->{
            taskService.deleteTask(1);
        });

        assertEquals("Task not found with id 1", exception.getMessage());
    }

    @Test
    void assignTaskAutomatically_assignsToUserWithLeastTasks(){
        //Arrange
        Task task = new Task();
        task.setRequiredQualification(Qualification.QualificationType.MID_LEVEL);
        task.setDepartment(new Department(1, "Development"));

        //2 tasks for user1
        User user1 = createUser(1, User.UserRole.EMPLOYEE, Qualification.QualificationType.MID_LEVEL, new Department(1, "Development"), 2);
        //1 task for user2
        User user2 = createUser(2, User.UserRole.EMPLOYEE, Qualification.QualificationType.MID_LEVEL, new Department(1, "Development"), 1);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        //Act
        Task createdTask = taskService.assignTaskAutomatically(task);

        //Assert
        assertNotNull(createdTask.getAssignedTo());
        assertEquals(user2, createdTask.getAssignedTo());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void assignTaskAutomatically_filtersUsersByQualificationAndDepartment() {
        // Arrange
        Task task = new Task();
        task.setRequiredQualification(Qualification.QualificationType.JUNIOR);
        task.setDepartment(new Department(1, "HR"));

        User user1 = createUser(1, User.UserRole.EMPLOYEE, Qualification.QualificationType.JUNIOR, new Department(1, "HR"), 2);
        User user2 = createUser(2, User.UserRole.EMPLOYEE, Qualification.QualificationType.MID_LEVEL, new Department(1, "Engineering"), 1);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task createdTask = taskService.assignTaskAutomatically(task);

        // Assert
        assertEquals(user1, createdTask.getAssignedTo());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void assignTaskAutomatically_throwsExceptionWhenNoQualifiedUsers() {
        // Arrange
        Task task = new Task();
        task.setRequiredQualification(Qualification.QualificationType.MID_LEVEL);
        task.setDepartment(new Department(1, "Development"));

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.assignTaskAutomatically(task);
        });
        assertEquals("No available users matching the task requirements", exception.getMessage());
    }
}
