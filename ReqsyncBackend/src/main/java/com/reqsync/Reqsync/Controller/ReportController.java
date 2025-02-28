package com.reqsync.Reqsync.Controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reqsync.Reqsync.CustomException.WrongAuthenticationCredentials;
import com.reqsync.Reqsync.Entity.ReportEntity;
import com.reqsync.Reqsync.Service.ReportService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
// @PreAuthorize("hasAuthority('USER')")
@RequestMapping("/api/reports")
public class ReportController {

    // Set the maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    @Autowired
    private ReportService reportService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadReport(@RequestBody MultipartFile pdf) {
        // Validate that the file is not null or empty
        if (pdf == null || pdf.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required and cannot be empty.");
        }

        // Validate file size
        if (pdf.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File size exceeds maximum limit of 10MB.");
        }

        // Validate that the file is a PDF by checking its content type
        if (!"application/pdf".equalsIgnoreCase(pdf.getContentType())) {
            return ResponseEntity.badRequest().body("Invalid file type. Only PDF files are accepted.");
        }

        try {
            reportService.savePdfFile(pdf);
            return ResponseEntity.ok("Report uploaded successfully!");
        } catch (WrongAuthenticationCredentials e) {
            return ResponseEntity.status(401).body(e.getMessage()); // Unauthorized
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Bad Request
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload report: " + e.getMessage());
        }
    }

    @GetMapping("/download/{reportId}")
    public ResponseEntity<Resource> downloadReportFile(@PathVariable Long reportId, HttpServletRequest request) {
        try {
            Resource resource = reportService.downloadReportFile(reportId);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception ex) {
                // Default to binary if type could not be determined
                contentType = "application/octet-stream";
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllReports() {
        try {
            List<ReportEntity> reports = reportService.getAllReportsForUser();
            if (reports.isEmpty()) {
                return ResponseEntity.ok("No reports found.");
            }
            return ResponseEntity.ok(reports);
        } catch (WrongAuthenticationCredentials e) {
            return ResponseEntity.status(401).body(e.getMessage()); // Unauthorized
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve reports: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable("id") Long reportId) {

        if (reportId == null) {
            return ResponseEntity.badRequest().body("The id is mandotary");
        }
        try {
            reportService.deleteReportById(reportId);
            return ResponseEntity.ok("Report deleted successfully!");
        } catch (WrongAuthenticationCredentials e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete report: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    // @PreAuthorize("hasAuthority('HOSPITAL')")
    public ResponseEntity<List<ReportEntity>> getWholeReports() {
        List<ReportEntity> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reports);
    }
}
