package com.ir.formgenerator.dto;

import com.ir.formgenerator.model.GenerationResult;

public class GenerationResultDto {

    private final String pdfFileName;
    private final String localDestination;
    private final String s3Destination;
    private final long localTransferMillis;
    private final long s3TransferMillis;
    private final String message;

    public GenerationResultDto(GenerationResult result) {
        this.pdfFileName = result.getPdfFileName();
        this.localDestination = result.getLocalDestination();
        this.s3Destination = result.getS3Destination();
        this.localTransferMillis = result.getLocalTransferMillis();
        this.s3TransferMillis = result.getS3TransferMillis();
        this.message = "PDF generated successfully.";
    }

    public String getPdfFileName() { return pdfFileName; }
    public String getLocalDestination() { return localDestination; }
    public String getS3Destination() { return s3Destination; }
    public long getLocalTransferMillis() { return localTransferMillis; }
    public long getS3TransferMillis() { return s3TransferMillis; }
    public String getMessage() { return message; }
}
