package com.reqsync.Reqsync.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "report_pdf")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "report_data", nullable = false)
    private String reportDataUrl;

    @Lob
    @Column(name = "report_data_text", columnDefinition = "LONGTEXT", nullable = false)
    private String reportData;
}