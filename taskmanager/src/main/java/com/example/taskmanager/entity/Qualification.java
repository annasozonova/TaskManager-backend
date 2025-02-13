package com.example.taskmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.util.Objects;

/**
 * Entity representing a user's qualification.
 */
@Entity
@Table(name = "qualifications")
public class Qualification {

    public enum QualificationType {
        JUNIOR,
        MID_LEVEL,
        SENIOR;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qualification_id_seq")
    @SequenceGenerator(name = "qualifications_id_seq", sequenceName = "qualification_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Min(value = 0, message = "Experience years must be at least 0")
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Column(name = "technologies", length = Integer.MAX_VALUE)
    private String technologies;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification", length = 50)
    private QualificationType qualification;

    /**
     * The user associated with this qualification.
     * This is a one-to-one relationship, and the "qualification" field in the User entity is the owning side.
     */
    @OneToOne(mappedBy = "qualification")
    private User user;

    //Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
    public String toString() {
        return "Qualification{" +
                "id=" + id +
                ", qualificationType='" + qualification + '\'' +
                ", years='" + experienceYears + '\'' +
                ", techno='" + technologies + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualification);
    }
}