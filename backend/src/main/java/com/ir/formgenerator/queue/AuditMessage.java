package com.ir.formgenerator.queue;

import java.io.Serializable;


public class AuditMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String csvFileInput;
    private String pdfFileOutput;
    private String fileDestination;


    public AuditMessage() {}

    public AuditMessage(String username, String csvFileInput,
                        String pdfFileOutput, String fileDestination) {
        this.username = username;
        this.csvFileInput = csvFileInput;
        this.pdfFileOutput = pdfFileOutput;
        this.fileDestination = fileDestination;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCsvFileInput() { return csvFileInput; }
    public void setCsvFileInput(String csvFileInput) { this.csvFileInput = csvFileInput; }

    public String getPdfFileOutput() { return pdfFileOutput; }
    public void setPdfFileOutput(String pdfFileOutput) { this.pdfFileOutput = pdfFileOutput; }

    public String getFileDestination() { return fileDestination; }
    public void setFileDestination(String fileDestination) { this.fileDestination = fileDestination; }

    @Override
    public String toString() {
        return "AuditMessage{username='" + username + "', csvFileInput='" + csvFileInput
                + "', pdfFileOutput='" + pdfFileOutput + "', fileDestination='" + fileDestination + "'}";
    }
}
