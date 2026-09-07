package com.ir.formgenerator.service;

import com.ir.formgenerator.model.AuditLog;
import com.ir.formgenerator.queue.AuditMessage;
import com.ir.formgenerator.queue.AuditProducer;
import com.ir.formgenerator.repository.AuditLogRepository;
import com.ir.formgenerator.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock private AuditProducer auditProducer;
    @Mock private AuditLogRepository auditLogRepository;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditProducer, auditLogRepository);
    }

    @Test
    void recordAsync_publishesMessageToProducer() {
        auditService.recordAsync("alice", "input.csv", "out.pdf", "/local/out.pdf");

        verify(auditProducer, times(1)).publish(any(AuditMessage.class));
    }

    @Test
    void recordAsync_messageContainsCorrectFields() {
        ArgumentCaptor<AuditMessage> captor = ArgumentCaptor.forClass(AuditMessage.class);

        auditService.recordAsync("bob", "data.csv", "report.pdf", "s3://bucket/report.pdf");

        verify(auditProducer).publish(captor.capture());
        AuditMessage captured = captor.getValue();

        assertThat(captured.getUsername()).isEqualTo("bob");
        assertThat(captured.getCsvFileInput()).isEqualTo("data.csv");
        assertThat(captured.getPdfFileOutput()).isEqualTo("report.pdf");
        assertThat(captured.getFileDestination()).isEqualTo("s3://bucket/report.pdf");
    }

    @Test
    void recordAsync_doesNotCallRepository_directlyOnPublish() {
        auditService.recordAsync("carol", "c.csv", "c.pdf", "/out/c.pdf");

        // Repository should NOT be called during publish — only the consumer persists
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void findAll_delegatesToRepository() {
        AuditLog log1 = new AuditLog("user1", "a.csv", "a.pdf", "/out/a.pdf", LocalDateTime.now());
        AuditLog log2 = new AuditLog("user2", "b.csv", "b.pdf", "/out/b.pdf", LocalDateTime.now().minusMinutes(5));
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(log1, log2));

        List<AuditLog> result = auditService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
    }

    @Test
    void findAll_returnsEmptyList_whenNoLogs() {
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of());

        List<AuditLog> result = auditService.findAll();

        assertThat(result).isEmpty();
    }
}
