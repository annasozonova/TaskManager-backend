package com.example.taskmanager.service;

import com.example.taskmanager.entity.*;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.aspectj.weaver.ast.Not;
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

/**
 * Service class for managing user-related operations, including user creation,
 * updating, deletion, and retrieval. It handles password encryption, user roles,
 * qualifications, and department assignments.
 */
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

    /**
     * Saves a new user to the repository with password encryption.
     * @param user User entity to be saved
     * @return Saved user
     */
    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Creates a new user based on the provided data and performs validation for
     * existing usernames and emails.
     * @param userData Data for creating the user
     * @return Created user
     * @throws IllegalArgumentException if username or email already exists
     */
    public User createUser(Map<String, Object> userData) {
        User user = processUser(userData, null);
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return saveUser(user);
    }

    /**
     * Updates an existing user based on the provided data.
     * @param id ID of the user to be updated
     * @param userData Data to update the user
     * @return Updated user
     * @throws ResourceNotFoundException if user is not found
     */
    public User updateUser(Integer id, Map<String, Object> userData) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        updateExistingUser(existingUser, userData);

        return saveUser(existingUser);
    }

    /**
     * Retrieves all users from the repository.
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by ID.
     * @param userId ID of the user to be retrieved
     * @return User with the specified ID
     * @throws ResourceNotFoundException if user is not found
     */
    public User getUserById(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return userRepository.findById(userId).get();
    }

    /**
     * Deletes a user by ID, removing task assignments and sending a notification.
     * @param id ID of the user to be deleted
     * @throws ResourceNotFoundException if user is not found
     */
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id " + id)
        );

        // Unassign tasks from the user
        List<Task> tasks = user.getTasks();
        for (Task task : tasks) {
            task.setAssignedTo(null);  // Unassign task
        }
        taskRepository.saveAll(tasks);

        // Delete user's notifications
        List<Notification> notifications = user.getNotifications();
        for (Notification notification : notifications) {
            notificationService.deleteNotification(notification.getId());
        }

        userRepository.deleteById(id);
        notificationService.sendAdminNotification("User deleted: " + user.getUsername(), Notification.NotificationType.USER, user.getId());
    }

    /**
     * Processes user data to create or update a user.
     * @param userData Data for user creation or update
     * @param id ID of the user (null for creation)
     * @return Processed user entity
     */
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

    /**
     * Updates an existing user with the provided data.
     * @param user User to be updated
     * @param userData Data to update the user
     */
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

    /**
     * Retrieves all users belonging to a specific department.
     * @param departmentId ID of the department
     * @return List of users in the specified department
     */
    public List<User> findUsersByDepartment(Integer departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }

    /**
     * Changes the user's password if the current password matches the existing one.
     * @param currentPassword Current password of the user
     * @param newPassword New password for the user
     * @return true if password was changed, false if not
     */
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

    /**
     * Finds a user by their username.
     * @param username Username of the user
     * @return User with the specified username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Updates the last login time for the user.
     * @param username Username of the user
     */
    public void updateUserLastLogin(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * Counts the total number of users in the system.
     * @return Total number of users
     */
    public Integer countAllUsers() {
        return Math.toIntExact(userRepository.count());
    }

    /**
     * Counts the number of active users in a specific department.
     * @param departmentId ID of the department
     * @return Number of active users in the department
     */
    public Integer countActiveUsersByDepartment(Integer departmentId) {
        return userRepository.countActiveUsersByDepartment(departmentId);
    }

    /**
     * Retrieves users who have been inactive for over a month.
     * @return List of inactive users
     */
    public List<User> findInactiveUsers() {
        return userRepository.findInactiveUsers(LocalDateTime.now().minusMonths(1));
    }
}
