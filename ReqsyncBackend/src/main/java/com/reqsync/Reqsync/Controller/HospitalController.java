package com.reqsync.Reqsync.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reqsync.Reqsync.Dto.HospitalFormDto;
import com.reqsync.Reqsync.Entity.ReportEntity;
import com.reqsync.Reqsync.Model.HospitalDataToJson;
import com.reqsync.Reqsync.Service.HospitalService;
import com.reqsync.Reqsync.Service.ReportService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/data")
    public ResponseEntity<String> getHospitalData() {
        String url = "https://dshm.delhi.gov.in/mis/(S(atqnz1epevjntznia4gxtqri))/Private/frmFreeBedMonitoringReport.aspx";
        List<Map<String, String>> hospitalData = HospitalService.scrapeHospitalData(url);
        String response = HospitalDataToJson.convertToJson(hospitalData);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/addHospital")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> addHospital(@RequestBody HospitalFormDto hospitalFormDto) {
        try {
            hospitalService.addHospital(hospitalFormDto);
            return ResponseEntity.ok("Hospital registration submitted successfully. Await manager verification.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error submitting hospital registration: " + ex.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('HOSPITAL')")
    @GetMapping("/all")
    public ResponseEntity<List<ReportEntity>> getAllReports() {
        List<ReportEntity> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reports);
    }

}