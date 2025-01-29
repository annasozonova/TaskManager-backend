package com.example.taskmanager.service;

import com.example.taskmanager.entity.Department;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.DepartmentRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentService(TaskRepository taskRepository, UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new department.
     * @param department The department to create.
     * @return The created department.
     */
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    /**
     * Retrieves all departments.
     * @return A list of all departments.
     */
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * Retrieves all tasks associated with a specific department.
     * @param departmentId The ID of the department.
     * @return A list of tasks assigned to users in the department.
     */
    public List<Task> getTasksByDepartmentId(Integer departmentId) {
        List<User> users = userRepository.getUsersByDepartment(departmentRepository.findById(departmentId).get());
        return users.stream()
                .flatMap(user -> taskRepository.findByAssignedTo(user).stream())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all users in a specific department.
     * @param departmentId The ID of the department.
     * @return A list of users in the department.
     */
    public List<User> getUsersByDepartmentId(int departmentId) {
        return userRepository.getUsersByDepartment(departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + departmentId)));
    }

    /**
     * Updates an existing department.
     * @param id The ID of the department to update.
     * @param department The department object containing updated details.
     * @return The updated department.
     */
    public Department updateDepartment(Integer id, Department department) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id " + id);
        }
        department.setId(id);
        return departmentRepository.save(department);
    }

    public Optional<Department> findDepartmentById(Integer id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id " + id);
        }
        return departmentRepository.findById(id);
    }
}
