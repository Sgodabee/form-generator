package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.service.TransferSpeedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ConsoleTransferSpeedLogger implements TransferSpeedLogger {

    private static final Logger log = LoggerFactory.getLogger(ConsoleTransferSpeedLogger.class);

    @Override
    public void logComparison(String fileName, long localMillis, long s3Millis) {
        log.info("=== Transfer Speed Comparison ===");
        log.info("  File            : {}", fileName);
        log.info("  Local store     : {} ms", localMillis);
        log.info("  Amazon S3 store : {} ms", s3Millis);

        String faster = localMillis <= s3Millis ? "Local" : "S3";
        long diff = Math.abs(localMillis - s3Millis);
        log.info("  Result          : {} was faster by {} ms", faster, diff);
        log.info("=================================");
    }
}
