package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignedTo(User user);

    List<Task> findByDepartmentId(Integer id);

    List<Task> findByDueDateBetween(LocalDate today, LocalDate threeDaysFromNow);

    Integer countByStatus(Task.TaskStatus status);
    List<Task> findByPriority(Task.TaskPriority priority);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByUser(@Param("userId") Integer userId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT t FROM Task t WHERE t.department.id = :departmentId AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByDepartment(@Param("departmentId") Integer departmentId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findAllOverdueTasks(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT u.id, u.username, COUNT(t) FROM User u LEFT JOIN Task t ON u.id = t.assignedTo.id WHERE u.department.id = :departmentId GROUP BY u.id, u.username")
    List<Object[]> getUserTaskLoadByDepartment(@Param("departmentId") Integer departmentId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.department.id = :departmentId")
    Integer countByDepartment(@Param("departmentId") Integer departmentId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId")
    Integer countByAssignedTo(@Param("userId") Integer userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = :status")
    Integer countByAssignedToAndStatus(@Param("userId") Integer userId, @Param("status") Task.TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.department.id = :departmentId AND t.status = :status")
    Integer countByDepartmentAndStatus(@Param("departmentId") Integer departmentId, @Param("status") Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.priority = :priority")
    List<Task> findByAssignedToAndPriority(@Param("userId") Integer userId, @Param("priority") Task.TaskPriority priority);

    @Query("SELECT t FROM Task t WHERE t.department.id = :departmentId AND t.priority = :priority")
    List<Task> findByDepartmentAndPriority(@Param("departmentId") Integer departmentId, @Param("priority") Task.TaskPriority priority);

    @Query("SELECT u.id, u.username, COUNT(t) FROM User u LEFT JOIN Task t ON u.id = t.assignedTo.id GROUP BY u.id, u.username")
    List<Object[]> getUserTaskLoadForAllDepartments();
}

