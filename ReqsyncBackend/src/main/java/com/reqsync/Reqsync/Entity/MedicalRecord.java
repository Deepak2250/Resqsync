// package com.reqsync.Reqsync.Entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// import java.time.LocalDateTime;

// @Entity
// @Table(name = "medical_records")
// @AllArgsConstructor
// @NoArgsConstructor
// @Getter
// @Setter
// public class MedicalRecord {

// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// private Long id;

// @ManyToOne
// @JoinColumn(name = "patient_id", nullable = false)
// private PatientEntity patient;

// // Reference to the doctor who resolved the case
// @ManyToOne
// @JoinColumn(name = "doctor_id", nullable = false)
// private DoctorEntity doctor;

// // Reference to the prescription provided by the doctor
// @OneToOne
// @JoinColumn(name = "prescription_id", nullable = false)
// private Prescription prescription;

// }