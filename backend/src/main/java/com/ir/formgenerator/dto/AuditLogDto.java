package com.ir.formgenerator.dto;

import com.ir.formgenerator.model.AuditLog;
import java.time.LocalDateTime;


public class AuditLogDto {

    private final Long id;
    private final String username;
    private final String csvFileInput;
    private final String pdfFileOutput;
    private final String fileDestination;
    private final LocalDateTime timestamp;

    public AuditLogDto(AuditLog log) {
        this.id = log.getId();
        this.username = log.getUsername();
        this.csvFileInput = log.getCsvFileInput();
        this.pdfFileOutput = log.getPdfFileOutput();
        this.fileDestination = log.getFileDestination();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getCsvFileInput() { return csvFileInput; }
    public String getPdfFileOutput() { return pdfFileOutput; }
    public String getFileDestination() { return fileDestination; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
