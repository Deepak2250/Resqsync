package com.reqsync.Reqsync.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reqsync.Reqsync.CustomException.DuplicateFileName;
import com.reqsync.Reqsync.CustomException.WrongAuthenticationCredentials;
import com.reqsync.Reqsync.Entity.ReportEntity;
import com.reqsync.Reqsync.Entity.User;
import com.reqsync.Reqsync.Repository.ReportRepository;
import com.reqsync.Reqsync.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReportService {

    @Value("${file.upload-dir}")
    private String uploadDir; // Configured in application.properties

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public ReportEntity savePdfFile(MultipartFile file) throws IOException {
        // Validate authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new WrongAuthenticationCredentials("User not authenticated. Please log in first.");
        }

        // Retrieve the authenticated user's email
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if a report with the same original file name already exists
        ReportEntity existingReport = reportRepository.findByFileName(file.getOriginalFilename());
        if (existingReport != null) {
            throw new DuplicateFileName("The same file is already present!");
        }

        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new IOException("Could not create upload directory: " + uploadDir, e);
        }

        // Generate a unique file name to avoid collisions
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Save the file to the file system
        try {
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new IOException("Failed to save file to disk.", e);
        }

        // Extract text from the PDF using PDFBox
        String extractedText;
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractedText = pdfStripper.getText(document);
        } catch (IOException e) {
            throw new IOException("Error extracting text from PDF.", e);
        }

        // Create and populate the ReportEntity with file information and extracted text
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(file.getOriginalFilename());
        // Save the unique filename rather than the full path so that FileStorageService
        // can load it.
        reportEntity.setReportDataUrl(uniqueFileName);
        reportEntity.setReportData(extractedText);
        reportEntity.setUser(user);

        // Save the ReportEntity to the database
        try {
            reportRepository.save(reportEntity);
        } catch (Exception e) {
            throw new RuntimeException("Error saving report to database.", e);
        }

        // Optionally update the user's list of reports
        try {
            List<ReportEntity> reports = user.getEntity();
            reports.add(reportEntity);
            user.setEntity(reports);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user's report list.", e);
        }

        return reportEntity;
    }

    // Method to fetch all reports for the authenticated user
    public List<ReportEntity> getAllReportsForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new WrongAuthenticationCredentials("User not authenticated. Please log in first.");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        try {
            return reportRepository.findByUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving reports for user.", e);
        }
    }

    @Transactional
    public void deleteReportById(Long reportId) throws Exception {
        // Check authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new WrongAuthenticationCredentials("User not authenticated. Please log in first.");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Retrieve the report by its ID
        ReportEntity reportEntity = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        // Verify that the report belongs to the authenticated user
        if (!reportEntity.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new IllegalArgumentException("Unauthorized: You are not allowed to delete this report.");
        }

        // Retrieve the file name from reportDataUrl and delete the file from the file
        // system if it exists
        String fileName = reportEntity.getReportDataUrl();
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new IOException("Error deleting file from disk.", e);
        }

        // Remove the report record from the database
        try {
            reportRepository.delete(reportEntity);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting report from database.", e);
        }
    }

    // Method to fetch all reports (for administrative purposes)
    public List<ReportEntity> getAllReports() {
        try {
            return reportRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving reports from database.", e);
        }
    }

    /**
     * New method to download (or open) a PDF file based on the report ID.
     * It checks authentication, verifies report ownership, and then uses the
     * FileStorageService
     * to load the file as a Resource.
     *
     * @param reportId the ID of the report to download.
     * @return the file as a Resource.
     * @throws Exception if any error occurs.
     */
    public Resource downloadReportFile(Long reportId) throws Exception {
        // Check authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new WrongAuthenticationCredentials("User not authenticated. Please log in first.");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Retrieve the report by its ID
        ReportEntity reportEntity = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        // Verify that the report belongs to the authenticated user
        if (!reportEntity.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new IllegalArgumentException("Unauthorized: You are not allowed to download this report.");
        }

        // Load the file as a Resource using the unique file name stored in
        // reportDataUrl
        try {
            return fileStorageService.loadFileAsResource(reportEntity.getReportDataUrl());
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("File not found for report id: " + reportId);
        }
    }
}
