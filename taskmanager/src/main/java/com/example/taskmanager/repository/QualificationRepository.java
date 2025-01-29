package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QualificationRepository extends JpaRepository<Qualification, Integer> {
}

