package com.ir.formgenerator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "csv_file_input", nullable = false)
    private String csvFileInput;

    @Column(name = "pdf_file_output", nullable = false)
    private String pdfFileOutput;

    @Column(name = "file_destination", nullable = false)
    private String fileDestination;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected AuditLog() {}

    public AuditLog(String username, String csvFileInput, String pdfFileOutput,
                    String fileDestination, LocalDateTime timestamp) {
        this.username = username;
        this.csvFileInput = csvFileInput;
        this.pdfFileOutput = pdfFileOutput;
        this.fileDestination = fileDestination;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getCsvFileInput() { return csvFileInput; }
    public String getPdfFileOutput() { return pdfFileOutput; }
    public String getFileDestination() { return fileDestination; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
