// package com.reqsync.Reqsync.Entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Getter
// @AllArgsConstructor
// @NoArgsConstructor
// @Setter
// @Table(name = "doctor_points")
// public class DoctorPointsEntity {

// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// private Long id;

// // Associate each DoctorPoints record with a unique DoctorEntity via
// // licenseNumber
// @OneToOne
// private DoctorEntity doctor;

// @Column(nullable = false)
// private Long points;

// }