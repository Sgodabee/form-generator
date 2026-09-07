package com.ir.formgenerator.service;

import com.ir.formgenerator.service.impl.ConsoleTransferSpeedLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class ConsoleTransferSpeedLoggerTest {

    private ConsoleTransferSpeedLogger logger;

    @BeforeEach
    void setUp() {
        logger = new ConsoleTransferSpeedLogger();
    }

    @Test
    void logComparison_localFaster_doesNotThrow() {
        assertThatCode(() -> logger.logComparison("form.pdf", 10L, 50L))
                .doesNotThrowAnyException();
    }

    @Test
    void logComparison_s3Faster_doesNotThrow() {
        assertThatCode(() -> logger.logComparison("form.pdf", 80L, 20L))
                .doesNotThrowAnyException();
    }

    @Test
    void logComparison_equalSpeed_doesNotThrow() {
        assertThatCode(() -> logger.logComparison("form.pdf", 30L, 30L))
                .doesNotThrowAnyException();
    }

    @Test
    void logComparison_zeroMillis_doesNotThrow() {
        assertThatCode(() -> logger.logComparison("form.pdf", 0L, 0L))
                .doesNotThrowAnyException();
    }

    @Test
    void logComparison_largeValues_doesNotThrow() {
        assertThatCode(() -> logger.logComparison("large_form.pdf", 5000L, 3000L))
                .doesNotThrowAnyException();
    }
}
