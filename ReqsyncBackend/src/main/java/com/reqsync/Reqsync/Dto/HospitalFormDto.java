package com.reqsync.Reqsync.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HospitalFormDto {
    private String registrationNumber;
    private String hospitalName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String officialEmail;
    private String phone;
    private String website;
}
