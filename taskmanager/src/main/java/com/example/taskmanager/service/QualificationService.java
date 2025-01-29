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
     * Create a new qualification record.
     * @param qualification The qualification record to create.
     * @return The saved qualification record.
     */
    @Transactional
    public Qualification createQualification(Qualification qualification) {
        return qualificationRepository.save(qualification);
    }

    public Qualification updateQualification(Integer id, Qualification qualification) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualification not found for id " + id);
        }
        qualification.setId(id);
        return qualificationRepository.save(qualification);
    }

    public List<Qualification> getAllQualifications() {
        return qualificationRepository.findAll();
    }

    public void deleteQualification(Integer id) {
        qualificationRepository.deleteById(id);
    }

    public Optional<Qualification> findQualificationById(Integer id) {
        if (!qualificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualification not found for id " + id);
        }
        return qualificationRepository.findById(id);
    }
}
