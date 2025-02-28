package com.reqsync.Reqsync.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Table(name = "hospital_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HospitalInfo {

    @Id
    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "official_email", nullable = false)
    private String officialEmail;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "website", nullable = false)
    private String website;

    @OneToOne
    @JoinColumn(name = "user_email")
    @JsonBackReference
    private User user;

    // Field to mark if the hospital has been verified
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
}
