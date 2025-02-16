package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.service.QualificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing qualifications in the system.
 * This controller provides endpoints to create, update, retrieve, and delete qualifications.
 */
@RestController
@RequestMapping("/api/qualifications")
public class QualificationController {
    private final QualificationService qualificationService;
    private final Logger logger = LoggerFactory.getLogger(QualificationController.class);

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    /**
     * Creates a new qualification.
     * @param qualification The qualification data to create.
     * @return The created qualification.
     */
    @PostMapping
    public ResponseEntity<Qualification> createQualification(@RequestBody Qualification qualification) {
        Qualification createdQualification = qualificationService.createQualification(qualification);
        logger.info("Created Qualification: {}", createdQualification);
        return new ResponseEntity<>(createdQualification, HttpStatus.CREATED);
    }

    /**
     * Updates an existing qualification.
     * @param id The ID of the qualification to update.
     * @param qualification The updated qualification data.
     * @return The updated qualification.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Qualification> updateQualification(@PathVariable Integer id, @RequestBody Qualification qualification) {
        Qualification updatedQualification = qualificationService.updateQualification(id, qualification);
        return new ResponseEntity<>(updatedQualification, HttpStatus.OK);
    }

    /**
     * Retrieves all qualifications.
     * @return A list of all qualifications.
     */
    @GetMapping
    public ResponseEntity<List<Qualification>> getAllQualifications() {
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        return new ResponseEntity<>(qualifications, HttpStatus.OK);
    }

    /**
     * Deletes a qualification by its ID.
     * @param id The ID of the qualification to delete.
     * @return A response indicating the qualification was deleted.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Qualification> deleteQualification(@PathVariable Integer id) {
        qualificationService.deleteQualification(id);
        return ResponseEntity.noContent().build();
    }
}