package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.model.FormRecord;
import com.ir.formgenerator.model.GenerationResult;
import com.ir.formgenerator.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class FormGenerationServiceImpl implements FormGenerationService {

    private static final Logger log = LoggerFactory.getLogger(FormGenerationServiceImpl.class);

    private final CsvReaderService csvReaderService;
    private final PdfGeneratorService pdfGeneratorService;
    private final FileStoreService localFileStore;
    private final FileStoreService s3FileStore;
    private final AuditService auditService;
    private final TransferSpeedLogger transferSpeedLogger;

    @Value("${app.csv.input-path}")
    private String csvInputPath;

    public FormGenerationServiceImpl(
            CsvReaderService csvReaderService,
            PdfGeneratorService pdfGeneratorService,
            @Qualifier("localFileStoreService") FileStoreService localFileStore,
            @Qualifier("s3FileStoreService") FileStoreService s3FileStore,
            AuditService auditService,
            TransferSpeedLogger transferSpeedLogger) {

        this.csvReaderService = csvReaderService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.localFileStore = localFileStore;
        this.s3FileStore = s3FileStore;
        this.auditService = auditService;
        this.transferSpeedLogger = transferSpeedLogger;
    }

    @Override
    public GenerationResult generate(String username) {

        log.info("Starting form generation for user: {}", username);

        // Step 1 – Read CSV
        List<FormRecord> records = csvReaderService.readCsv(csvInputPath);
        log.info("Parsed {} record(s) from CSV: {}", records.size(), csvInputPath);

        // Step 2 – Generate PDF bytes
        byte[] pdfBytes = pdfGeneratorService.generate(records);

        // Step 3 – Build a timestamped output file name
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .withZone(java.time.ZoneId.systemDefault())
                .format(Instant.now());
        String pdfFileName = "form_" + timestamp + ".pdf";

        // Step 4 – Save to local store (timed)
        long localStart = System.currentTimeMillis();
        String localDestination = localFileStore.save(pdfFileName, pdfBytes);
        long localMillis = System.currentTimeMillis() - localStart;

        // Step 5 – Save to S3 (timed)
        long s3Start = System.currentTimeMillis();
        String s3Destination = s3FileStore.save(pdfFileName, pdfBytes);
        long s3Millis = System.currentTimeMillis() - s3Start;

        // Step 6 – Log speed comparison to console
        transferSpeedLogger.logComparison(pdfFileName, localMillis, s3Millis);

        // Step 7 – Enqueue audit event (non-blocking)
        auditService.recordAsync(username, csvInputPath, pdfFileName, localDestination);

        log.info("Form generation complete: {}", pdfFileName);

        return new GenerationResult(pdfFileName, localDestination, s3Destination,
                localMillis, s3Millis);
    }
}
