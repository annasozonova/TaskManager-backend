package com.example.taskmanager.service;

import com.example.taskmanager.entity.*;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final QualificationService qualificationService;
    private final DepartmentService departmentService;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository,
                       QualificationService qualificationService,
                       DepartmentService departmentService, TaskRepository taskRepository, BCryptPasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.qualificationService = qualificationService;
        this.departmentService = departmentService;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User createUser(Map<String, Object> userData) {
        User user = processUser(userData, null);
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        notificationService.sendAdminNotification("New user registered: " + user.getUsername(), Notification.NotificationType.USER, user.getId());
        return saveUser(user);
    }

    public User updateUser(Integer id, Map<String, Object> userData) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        updateExistingUser(existingUser, userData);
        notificationService.sendAdminNotification("New user update registered: " + existingUser.getUsername(), Notification.NotificationType.USER, existingUser.getId());

        return saveUser(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return userRepository.findById(userId).get();
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id " + id)
        );

        // Отменяем назначение задач пользователю
        List<Task> tasks = user.getTasks();
        for (Task task : tasks) {
            task.setAssignedTo(null);  // Оставляем задачу неназначенной
        }
        taskRepository.saveAll(tasks);

        userRepository.deleteById(id);
        notificationService.sendAdminNotification("User deleted: " + user.getUsername(), Notification.NotificationType.USER, user.getId());
    }

    private User processUser(Map<String, Object> userData, Integer id) {
        Map<String, Object> qualificationData = (Map<String, Object>) userData.get("qualification");
        Qualification qualification = new Qualification();
        qualification.setQualification(Qualification.QualificationType.valueOf((String) qualificationData.get("qualification")));
        qualification.setTechnologies((String) qualificationData.get("technologies"));
        qualification.setExperienceYears((Integer) qualificationData.get("experienceYears"));

        qualificationService.createQualification(qualification);
        logger.info("Created qualification: " + qualification.toString());

        User user = new User();
        user.setUsername((String) userData.get("username"));
        user.setEmail((String) userData.get("email"));
        user.setPassword((String) userData.get("password"));
        user.setFirstName((String) userData.get("firstName"));
        user.setLastName((String) userData.get("lastName"));
        user.setRole(User.UserRole.valueOf((String) userData.get("role")));
        user.setQualification(qualification);
        user.setLastLogin(LocalDateTime.now());

        Map<String, Object> departmentData = (Map<String, Object>) userData.get("department");
        Department department = departmentService.findDepartmentById((Integer) departmentData.get("id"))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with that ID"));
        user.setDepartment(department);

        if (id != null) {
            user.setId(id);
        }

        return user;
    }

    private void updateExistingUser(User user, Map<String, Object> userData) {
        if (userData.containsKey("username")) user.setUsername((String) userData.get("username"));
        if (userData.containsKey("email")) user.setEmail((String) userData.get("email"));
        if (userData.containsKey("password")) user.setPassword((String) userData.get("password"));
        if (userData.containsKey("firstName")) user.setFirstName((String) userData.get("firstName"));
        if (userData.containsKey("lastName")) user.setLastName((String) userData.get("lastName"));
        if (userData.containsKey("role")) user.setRole(User.UserRole.valueOf((String) userData.get("role")));

        if (userData.containsKey("qualification")) {
            Map<String, Object> qualificationData = (Map<String, Object>) userData.get("qualification");
            Qualification qualification = user.getQualification();
            if (qualification == null) {
                qualification = new Qualification();
            }
            qualification.setQualification(Qualification.QualificationType.valueOf((String) qualificationData.get("qualification")));
            qualification.setTechnologies((String) qualificationData.get("technologies"));
            qualification.setExperienceYears((Integer) qualificationData.get("experienceYears"));

            qualificationService.createQualification(qualification);
            user.setQualification(qualification);
        }

        if (userData.containsKey("department")) {
            Map<String, Object> departmentData = (Map<String, Object>) userData.get("department");
            Department department = departmentService.findDepartmentById((Integer) departmentData.get("id"))
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with that ID"));
            user.setDepartment(department);
        }
    }

    public List<User> findUsersByDepartment(Integer departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }

    public boolean changePassword(String currentPassword, String newPassword) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username);

        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateUserLastLogin(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}