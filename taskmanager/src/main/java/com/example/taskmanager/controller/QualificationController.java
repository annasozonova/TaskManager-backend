package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Qualification;
import com.example.taskmanager.service.QualificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/qualifications")
public class QualificationController {
    private final QualificationService qualificationService;
    private final Logger logger = LoggerFactory.getLogger(QualificationController.class);

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @PostMapping
    public ResponseEntity<Qualification> createQualification(@RequestBody Qualification qualification) {
        Qualification createdQualification = qualificationService.createQualification(qualification);
        logger.info("Created Qualification: {}", createdQualification);
        return new ResponseEntity<>(createdQualification, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Qualification> updateQualification(@PathVariable Integer id, @RequestBody Qualification qualification) {
        Qualification updatedQualification = qualificationService.updateQualification(id, qualification);
        return new ResponseEntity<>(updatedQualification, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Qualification>> getAllQualifications() {
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        return new ResponseEntity<>(qualifications, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Qualification> deleteQualification(@PathVariable Integer id) {
        qualificationService.deleteQualification(id);
        return ResponseEntity.noContent().build();
    }
}
