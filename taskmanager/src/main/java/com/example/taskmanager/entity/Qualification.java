package com.example.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;

@Entity
@Table(name = "qualifications")
public class Qualification {

    public enum QualificationType {
        JUNIOR,
        MID_LEVEL,
        SENIOR;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qualifications_id_seq")
    @SequenceGenerator(name = "qualifications_id_seq", sequenceName = "qualifications_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @JsonBackReference
    @ManyToOne( optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(value = 0, message = "Experience years must be at least 0")
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;


    @Column(name = "technologies", length = Integer.MAX_VALUE)
    private String technologies;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification", length = 50)
    private QualificationType qualification;

    //Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public QualificationType getQualification() {
        return qualification;
    }

    public void setQualification(QualificationType qualification) {
        this.qualification = qualification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Qualification that = (Qualification) o;
        return qualification == that.qualification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualification);
    }
}