package com.reqsync.Reqsync.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.reqsync.Reqsync.CustomException.NoHeader;
import com.reqsync.Reqsync.CustomException.NoRowsFound;
import com.reqsync.Reqsync.CustomException.NoTableFound;
import com.reqsync.Reqsync.CustomException.WrongAuthenticationCredentials;
import com.reqsync.Reqsync.Dto.HospitalFormDto;
import com.reqsync.Reqsync.Entity.HospitalInfo;
import com.reqsync.Reqsync.Entity.Roles;
import com.reqsync.Reqsync.Entity.User;
import com.reqsync.Reqsync.Repository.HospitalInfoRepository;
import com.reqsync.Reqsync.Repository.RoleRepository;
import com.reqsync.Reqsync.Repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalService {

    @Autowired
    private HospitalInfoRepository hospitalInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService; // Optional: for notifications

    public static List<Map<String, String>> scrapeHospitalData(String url) {
        List<Map<String, String>> hospitalData = new ArrayList<>();

        try {
            // Fetch the HTML page
            Document document = Jsoup.connect(url).get();

            // Locate the table (adjust the selector based on the actual HTML structure)
            Element table = document.select("table").first();
            if (table != null) {
                // Extract table headers (column names)
                Elements headers = table.select("tr th"); // Adjust if headers are in a different structure
                List<String> columnNames = new ArrayList<>();
                if (!headers.isEmpty()) {
                    for (Element header : headers) {
                        columnNames.add(header.text());
                    }
                } else {
                    throw new NoHeader("No headers found in the table.");

                }

                // Extract table rows
                Elements rows = table.select("tr"); // Adjust if rows are in a different structure
                if (!rows.isEmpty()) {
                    for (Element row : rows) {
                        Elements columns = row.select("td");
                        if (!columns.isEmpty() && columns.size() == columnNames.size()) {
                            Map<String, String> rowData = new HashMap<>();
                            for (int i = 0; i < columns.size(); i++) {
                                rowData.put(columnNames.get(i), columns.get(i).text());
                            }
                            hospitalData.add(rowData);
                        }
                    }
                } else {
                    throw new NoRowsFound("No rows found in the table.");
                }
            } else {
                throw new NoTableFound("No table found on the page.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hospitalData;
    }

    @Transactional
    public void addHospital(HospitalFormDto hospitalFormDto) {
        // Validate required fields
        if (hospitalFormDto.getRegistrationNumber() == null
                || hospitalFormDto.getRegistrationNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        if (hospitalFormDto.getHospitalName() == null || hospitalFormDto.getHospitalName().trim().isEmpty()) {
            throw new IllegalArgumentException("Hospital name is required.");
        }
        if (hospitalFormDto.getAddress() == null || hospitalFormDto.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required.");
        }
        if (hospitalFormDto.getCity() == null || hospitalFormDto.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required.");
        }
        if (hospitalFormDto.getState() == null || hospitalFormDto.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required.");
        }
        if (hospitalFormDto.getZipCode() == null || hospitalFormDto.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code is required.");
        }
        if (hospitalFormDto.getOfficialEmail() == null || hospitalFormDto.getOfficialEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Official email is required.");
        }
        if (hospitalFormDto.getPhone() == null || hospitalFormDto.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (hospitalFormDto.getWebsite() == null || hospitalFormDto.getWebsite().trim().isEmpty()) {
            throw new IllegalArgumentException("Website is required.");
        }

        // Check if the current user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new WrongAuthenticationCredentials("User not authenticated. Please log in first.");
        }
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        // Retrieve roles from repository
        Roles hospitalRole = roleRepository.findByRole("HOSPITAL");
        Roles volunteerRole = roleRepository.findByRole("VOLUNTEER");
        Roles helpRequesterRole = roleRepository.findByRole("HELPREQUESTER");
        if (hospitalRole == null) {
            throw new IllegalArgumentException("HOSPITAL role not found.");
        }
        // If user already has any conflicting role, disallow registration as hospital.
        if (user.getRoles().contains(hospitalRole)) {
            throw new IllegalArgumentException("User already has the HOSPITAL role.");
        }
        if (user.getRoles().contains(volunteerRole)) {
            throw new IllegalArgumentException(
                    "User already has the VOLUNTEER role and cannot register as a hospital.");
        }
        if (user.getRoles().contains(helpRequesterRole)) {
            throw new IllegalArgumentException(
                    "User already has the HELP_REQUESTER role and cannot register as a hospital.");
        }

        // Optionally check if a hospital with the same registration number already
        // exists
        if (hospitalInfoRepository.existsById(hospitalFormDto.getRegistrationNumber())) {
            throw new IllegalArgumentException("Hospital with the given registration number already exists.");
        }

        // Create and populate the HospitalInfo entity from the DTO
        HospitalInfo hospitalInfo = new HospitalInfo();
        hospitalInfo.setRegistrationNumber(hospitalFormDto.getRegistrationNumber());
        hospitalInfo.setHospitalName(hospitalFormDto.getHospitalName());
        hospitalInfo.setAddress(hospitalFormDto.getAddress());
        hospitalInfo.setCity(hospitalFormDto.getCity());
        hospitalInfo.setState(hospitalFormDto.getState());
        hospitalInfo.setZipCode(hospitalFormDto.getZipCode());
        hospitalInfo.setOfficialEmail(hospitalFormDto.getOfficialEmail());
        hospitalInfo.setPhone(hospitalFormDto.getPhone());
        hospitalInfo.setWebsite(hospitalFormDto.getWebsite());
        hospitalInfo.setUser(user);
        hospitalInfo.setVerified(false); // Pending manager verification

        // Save the hospital registration request
        hospitalInfoRepository.save(hospitalInfo);

        emailService.sendVolunteerFormFilledUpEmail(userEmail, user.getName());
    }

}