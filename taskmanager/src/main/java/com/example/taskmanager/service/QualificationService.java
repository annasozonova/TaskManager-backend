package com.example.taskmanager.service;

import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.QualificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QualificationService {

    private final QualificationRepository qualificationRepository;

    @Autowired
    public QualificationService(QualificationRepository qualificationRepository) {
        this.qualificationRepository = qualificationRepository;
    }

    /**
     * Create a new qualification record.
     * @param qualification The qualification record to create.
     * @return The saved qualification record.
     */
    public Qualification createQualification(Qualification qualification) {
        return qualificationRepository.save(qualification);
    }

    /**
     * Retrieve qualification by user id.
     * @param userId The id of the user whose qualification is to be fetched.
     * @return The qualification of the user.
     */
    public Qualification getQualificationByUserId(Integer userId) {
        Optional<Qualification> qualification = qualificationRepository.findByUserId(userId);
        return qualification.orElseThrow(() -> new ResourceNotFoundException("Qualification not found for user with id " + userId));
    }
}
