package com.ir.formgenerator.queue;

import com.ir.formgenerator.model.AuditLog;
import com.ir.formgenerator.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditConsumerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditConsumer auditConsumer;

    @BeforeEach
    void setUp() {
        auditConsumer = new AuditConsumer(auditLogRepository);
    }

    @Test
    void consume_validMessage_savesAuditLog() {
        AuditMessage message = new AuditMessage(
                "alice", "input.csv", "output.pdf", "/local/output.pdf");

        auditConsumer.consume(message);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void consume_validMessage_persistsCorrectFields() {
        AuditMessage message = new AuditMessage(
                "bob", "data.csv", "report.pdf", "s3://bucket/report.pdf");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        auditConsumer.consume(message);

        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("bob");
        assertThat(saved.getCsvFileInput()).isEqualTo("data.csv");
        assertThat(saved.getPdfFileOutput()).isEqualTo("report.pdf");
        assertThat(saved.getFileDestination()).isEqualTo("s3://bucket/report.pdf");
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    void consume_setsTimestamp_toNow() {
        AuditMessage message = new AuditMessage(
                "carol", "c.csv", "c.pdf", "/out/c.pdf");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        auditConsumer.consume(message);

        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTimestamp()).isNotNull();
    }
}
