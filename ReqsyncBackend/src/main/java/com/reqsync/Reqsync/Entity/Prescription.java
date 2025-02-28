// package com.reqsync.Reqsync.Entity;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.Lob;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Table(name = "prescriptions")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// public class Prescription {

// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// private Long id;

// @ManyToOne
// @JoinColumn(name = "patient_id", nullable = false)
// private PatientEntity patient;

// // One-to-one association to ensure each doctor only has one prescription
// // record.
// @OneToOne(fetch = FetchType.LAZY)
// @JoinColumn(name = "doctor_id", nullable = false, unique = true)
// private DoctorEntity doctor;

// @Lob
// @Column(columnDefinition = "LONGBLOB", nullable = false)
// private String prescriptionText;

// }