package com.example.taskmanager.service;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user, checking for duplicate username and email.
     * @param user The user to register.
     * @return The registered user.
     * @throws IllegalArgumentException if the username or email already exists.
     */
    public User registerUser(User user){
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's details.
     * @param id The ID of the user to update.
     * @param user The user object containing updated details.
     * @return The updated user.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    public User updateUser(Integer id, User user) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        user.setId(id);
        return userRepository.save(user);
    }

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return userRepository.findById(userId).get();
    }
}
