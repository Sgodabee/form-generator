package com.ir.formgenerator.model;


public class GenerationResult {

    private final String pdfFileName;
    private final String localDestination;
    private final String s3Destination;
    private final long localTransferMillis;
    private final long s3TransferMillis;

    public GenerationResult(String pdfFileName,
                            String localDestination,
                            String s3Destination,
                            long localTransferMillis,
                            long s3TransferMillis) {
        this.pdfFileName = pdfFileName;
        this.localDestination = localDestination;
        this.s3Destination = s3Destination;
        this.localTransferMillis = localTransferMillis;
        this.s3TransferMillis = s3TransferMillis;
    }

    public String getPdfFileName() { return pdfFileName; }
    public String getLocalDestination() { return localDestination; }
    public String getS3Destination() { return s3Destination; }
    public long getLocalTransferMillis() { return localTransferMillis; }
    public long getS3TransferMillis() { return s3TransferMillis; }
}
