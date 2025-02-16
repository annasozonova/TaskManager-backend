package com.example.taskmanager.service;

import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.DepartmentRepository;
import com.example.taskmanager.repository.QualificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class that handles business logic related to qualifications.
 * It provides methods for creating, updating, retrieving, and deleting qualification records.
 */
@Service
public class QualificationService {

    private final QualificationRepository qualificationRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public QualificationService(QualificationRepository qualificationRepository, DepartmentRepository departmentRepository) {
        this.qualificationRepository = qualificationRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new qualification record.
     * This method saves a qualification record into the database.
     *
     * @param qualification The qualification record to create.
     * @return The saved qualification record.
     */
    @Transactional
    public Qualification createQualification(Qualification qualification) {
        return qualificationRepository.save(qualification);
    }

    /**
     * Updates an existing qualification record.
     * This method checks if the qualification with the given ID exists,
     * then updates and saves the qualification record.
     *
     * @param id The ID of the qualification to update.
     * @param qualification The qualification object containing updated details.
     * @return The updated qualification record.
     * @throws ResourceNotFoundException If the qualification with the given ID is not found.
     */
    public Qualification updateQualification(Integer id, Qualification qualification) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualification not found for id " + id);
        }
        qualification.setId(id);
        return qualificationRepository.save(qualification);
    }

    /**
     * Retrieves all qualification records.
     * This method returns a list of all qualifications in the system.
     *
     * @return A list of all qualifications.
     */
    public List<Qualification> getAllQualifications() {
        return qualificationRepository.findAll();
    }

    /**
     * Deletes a qualification record by its ID.
     * This method removes the qualification with the given ID from the database.
     *
     * @param id The ID of the qualification to delete.
     */
    public void deleteQualification(Integer id) {
        qualificationRepository.deleteById(id);
    }

    /**
     * Finds a qualification record by its ID.
     * This method returns the qualification if it exists, or throws an exception if not found.
     *
     * @param id The ID of the qualification to find.
     * @return An Optional containing the qualification if found.
     * @throws ResourceNotFoundException If the qualification with the given ID is not found.
     */
    public Optional<Qualification> findQualificationById(Integer id) {
        if (!qualificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualification not found for id " + id);
        }
        return qualificationRepository.findById(id);
    }
}
