package com.reqsync.Reqsync.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reqsync.Reqsync.Dto.HospitalInfoRoleAccessDto;
import com.reqsync.Reqsync.Dto.VolunteerDtoRoleAccess;
import com.reqsync.Reqsync.Entity.HospitalInfo;
import com.reqsync.Reqsync.Entity.Volunteer;
import com.reqsync.Reqsync.Service.ManagerService;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    // Retrieve all volunteer details (or requests) for manager review
    @GetMapping("/volunteers")
    public ResponseEntity<List<VolunteerDtoRoleAccess>> getAllVolunteers() {
        try {
            List<VolunteerDtoRoleAccess> volunteers = managerService.getAllVolunteers();
            return ResponseEntity.ok(volunteers);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Retrieve all hospital details (or requests) for manager review
    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalInfoRoleAccessDto>> getAllHospitals() {
        try {
            List<HospitalInfoRoleAccessDto> hospitals = managerService.getAllHospitals();
            return ResponseEntity.ok(hospitals);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Verify a volunteer and assign the VOLUNTEER role
    @PostMapping("/volunteers/verify")
    public ResponseEntity<String> verifyVolunteer(@RequestParam("email") String email) {
        try {
            managerService.verifyVolunteer(email);
            return ResponseEntity.ok("Volunteer verified successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error verifying volunteer: " + ex.getMessage());
        }
    }

    // Unverify a volunteer and remove the VOLUNTEER role if necessary
    @PostMapping("/volunteers/unverify")
    public ResponseEntity<String> unverifyVolunteer(@RequestParam("email") String email) {
        try {
            managerService.unverifyVolunteer(email);
            return ResponseEntity.ok("Volunteer unverified successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error unverifying volunteer: " + ex.getMessage());
        }
    }

    // Verify a hospital and assign the HOSPITAL role
    @PostMapping("/hospitals/verify")
    public ResponseEntity<String> verifyHospital(@RequestParam("email") String email) {
        try {
            managerService.verifyHospital(email);
            return ResponseEntity.ok("Hospital verified successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error verifying hospital: " + ex.getMessage());
        }
    }

    // Unverify a hospital and remove the HOSPITAL role if necessary
    @PostMapping("/hospitals/unverify")
    public ResponseEntity<String> unverifyHospital(@RequestParam("email") String email) {
        try {
            managerService.unverifyHospital(email);
            return ResponseEntity.ok("Hospital unverified successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error unverifying hospital: " + ex.getMessage());
        }
    }
}
