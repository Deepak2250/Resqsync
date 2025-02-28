// package com.reqsync.Reqsync.Entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// import java.time.LocalDate;
// import java.util.List;

// @Entity
// @Getter
// @AllArgsConstructor
// @NoArgsConstructor
// @Setter
// @Table(name = "patient_info")
// public class PatientEntity {

// @Id
// @Column(unique = true, nullable = false)
// private String identityNumber; // Aadhaar, Health Card, Passport Number
// (Unique)

// @Column(nullable = false)
// private LocalDate dateOfBirth;

// @Lob
// @Column(columnDefinition = "LONGBLOB", nullable = false)
// private String currentHealthIssue; // Short description of the problem

// @Column(nullable = false)
// private Long symptomDuration; // in days

// @Column(nullable = false)
// private int painLevel; // Scale 1-10

// @Column(nullable = false)
// private String emergencyContactPhone;

// @Column(name = "patient_medical_history", nullable = true)
// @ElementCollection()
// private List<String> medicalHistory; // Past surgeries, chronic conditions,
// etc.

// @Column(name = "is_Resolved", nullable = false)
// private boolean isResolved = false;

// @ManyToOne
// @JoinColumn(name = "user_email", nullable = false)
// private User user; // A patient is associated with a user

// @ManyToOne
// @JoinColumn(name = "doctor_license_number", nullable = true)
// private DoctorEntity doctor;

// }