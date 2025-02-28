package com.reqsync.Reqsync.Service;

import com.reqsync.Reqsync.Dto.HospitalInfoRoleAccessDto;
import com.reqsync.Reqsync.Dto.VolunteerDto;
import com.reqsync.Reqsync.Dto.VolunteerDtoRoleAccess;
import com.reqsync.Reqsync.Entity.HospitalInfo;
import com.reqsync.Reqsync.Entity.Roles;
import com.reqsync.Reqsync.Entity.User;
import com.reqsync.Reqsync.Entity.Volunteer;
import com.reqsync.Reqsync.Repository.HospitalInfoRepository;
import com.reqsync.Reqsync.Repository.RoleRepository;
import com.reqsync.Reqsync.Repository.UserRepository;
import com.reqsync.Reqsync.Repository.VolunteerRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private HospitalInfoRepository hospitalInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void verifyVolunteer(String email) {
        // Find the volunteer by user email
        Volunteer volunteer = volunteerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with email: " + email));

        // Check if the volunteer is already verified
        if (volunteer.isVerified()) {
            throw new RuntimeException("Volunteer is already verified.");
        }

        User user = volunteer.getUser();
        Roles volunteerRole = roleRepository.findByRole("VOLUNTEER");
        Roles helpRequesterRole = roleRepository.findByRole("HELPREQUESTER");
        Roles hospitalRole = roleRepository.findByRole("HOSPITAL");

        // Check if the user already has conflicting roles
        if (user.getRoles().contains(helpRequesterRole)) {
            throw new IllegalArgumentException(
                    "User is already a help requester and cannot be verified as a volunteer.");
        }
        if (user.getRoles().contains(hospitalRole)) {
            throw new IllegalArgumentException("User is already a hospital and cannot be verified as a volunteer.");
        }

        // Mark volunteer as verified and assign the VOLUNTEER role
        volunteer.setVerified(true);
        volunteerRepository.save(volunteer);

        if (!user.getRoles().contains(volunteerRole)) {
            user.getRoles().add(volunteerRole);
            userRepository.save(user);
        }

        // Send welcome email to the newly verified volunteer
        emailService.sendVolunteerWelcomeEmail(user.getEmail(), user.getName());
    }

    @Transactional
    public void unverifyVolunteer(String email) {
        // Find the volunteer by user email
        Volunteer volunteer = volunteerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with email: " + email));

        // Unverify the volunteer
        volunteer.setVerified(false);
        volunteerRepository.save(volunteer);

        // Optionally remove the VOLUNTEER role from the user
        User user = volunteer.getUser();
        Roles volunteerRole = roleRepository.findByRole("VOLUNTEER");
        if (user.getRoles().contains(volunteerRole)) {
            user.getRoles().remove(volunteerRole);
            userRepository.save(user);
        }
    }

    @Transactional
    public void verifyHospital(String email) {
        // Find the hospital by user email
        HospitalInfo hospitalInfo = hospitalInfoRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Hospital not found with email: " + email));

        // Verify the hospital
        hospitalInfo.setVerified(true);
        hospitalInfoRepository.save(hospitalInfo);

        // Assign the HOSPITAL role to the user if not already assigned
        User user = hospitalInfo.getUser();
        Roles hospitalRole = roleRepository.findByRole("HOSPITAL");
        if (!user.getRoles().contains(hospitalRole)) {
            user.getRoles().add(hospitalRole);
            userRepository.save(user);
        }
    }

    @Transactional
    public void unverifyHospital(String email) {
        // Find the hospital by user email
        HospitalInfo hospitalInfo = hospitalInfoRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Hospital not found with email: " + email));

        // Unverify the hospital
        hospitalInfo.setVerified(false);
        hospitalInfoRepository.save(hospitalInfo);

        // Optionally remove the HOSPITAL role from the user
        User user = hospitalInfo.getUser();
        Roles hospitalRole = roleRepository.findByRole("HOSPITAL");
        if (user.getRoles().contains(hospitalRole)) {
            user.getRoles().remove(hospitalRole);
            userRepository.save(user);
        }
    }

    // Retrieve all volunteer details (or requests) for the manager's review
    public List<VolunteerDtoRoleAccess> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        if (volunteers.isEmpty()) {
            return Collections.emptyList();
        }
        return volunteers.stream().map(v -> {
            VolunteerDtoRoleAccess dto = new VolunteerDtoRoleAccess();
            dto.setId(v.getId());
            dto.setUserEmail(v.getUser().getEmail());
            dto.setName(v.getName());
            dto.setPhone(v.getPhone());
            dto.setArea(v.getArea());
            dto.setAbout(v.getAbout());
            dto.setVolunteeringTypes(v.getVolunteeringTypes());
            dto.setSkills(v.getSkills());
            dto.setVerified(v.isVerified());
            return dto;
        }).collect(Collectors.toList());
    }

    // Retrieve all hospital details (or requests) for the manager's review
    public List<HospitalInfoRoleAccessDto> getAllHospitals() {
        List<HospitalInfo> hospitals = hospitalInfoRepository.findAll();
        if (hospitals.isEmpty()) {
            return Collections.emptyList();
        }
        return hospitals.stream().map(h -> {
            HospitalInfoRoleAccessDto dto = new HospitalInfoRoleAccessDto();
            dto.setRegistrationNumber(h.getRegistrationNumber());
            dto.setHospitalName(h.getHospitalName());
            dto.setAddress(h.getAddress());
            dto.setCity(h.getCity());
            dto.setState(h.getState());
            dto.setZipCode(h.getZipCode());
            dto.setOfficialEmail(h.getOfficialEmail());
            dto.setPhone(h.getPhone());
            dto.setWebsite(h.getWebsite());
            dto.setUserEmail(h.getUser().getEmail());
            dto.setVerified(h.isVerified());
            return dto;
        }).collect(Collectors.toList());
    }
}
