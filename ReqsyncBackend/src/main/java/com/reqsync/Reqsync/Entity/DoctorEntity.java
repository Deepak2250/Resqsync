// package com.reqsync.Reqsync.Entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// import java.util.List;

// @Entity
// @Getter
// @AllArgsConstructor
// @NoArgsConstructor
// @Setter
// @Table(name = "doctor_info")
// public class DoctorEntity {

// @Id
// @Column(unique = true, nullable = false)
// private String licenseNumber; // Unique license number of the doctor

// @Column(nullable = false)
// @ElementCollection
// private List<String> specialization; // e.g., Cardiologist, General
// Physician, etc.

// @Column(nullable = false)
// private String hospitalAffiliation; // Hospital or clinic name

// @Column(name = "office_address", nullable = false)
// private String workAddress; // Doctor's office address

// @OneToOne
// @JoinColumn(name = "user_email", nullable = false)
// private User user; // A doctor is associated with a user

// @OneToMany(mappedBy = "doctor")
// private List<PatientEntity> patients;

// @OneToOne(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Prescription prescriptions; // A doctor can write one prescriptions
// }