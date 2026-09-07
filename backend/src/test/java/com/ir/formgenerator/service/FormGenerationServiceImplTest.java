package com.ir.formgenerator.service;

import com.ir.formgenerator.model.FormRecord;
import com.ir.formgenerator.model.GenerationResult;
import com.ir.formgenerator.service.impl.FormGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormGenerationServiceImplTest {

    @Mock private CsvReaderService csvReaderService;
    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private FileStoreService localFileStore;
    @Mock private FileStoreService s3FileStore;
    @Mock private AuditService auditService;
    @Mock private TransferSpeedLogger transferSpeedLogger;

    private FormGenerationServiceImpl formGenerationService;

    private static final String TEST_CSV_PATH = "/input/sample.csv";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        formGenerationService = new FormGenerationServiceImpl(
                csvReaderService,
                pdfGeneratorService,
                localFileStore,
                s3FileStore,
                auditService,
                transferSpeedLogger
        );
        // inject the @Value field
        ReflectionTestUtils.setField(formGenerationService, "csvInputPath", TEST_CSV_PATH);
    }

    @Test
    void generate_happyPath_returnsGenerationResult() {
        // Arrange
        List<FormRecord> records = List.of(new FormRecord(Map.of("name", "Alice")));
        byte[] pdfBytes = "%PDF-test".getBytes();

        when(csvReaderService.readCsv(TEST_CSV_PATH)).thenReturn(records);
        when(pdfGeneratorService.generate(records)).thenReturn(pdfBytes);
        when(localFileStore.save(anyString(), eq(pdfBytes))).thenReturn("/local/form.pdf");
        when(s3FileStore.save(anyString(), eq(pdfBytes))).thenReturn("s3://bucket/form.pdf");

        // Act
        GenerationResult result = formGenerationService.generate(TEST_USERNAME);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPdfFileName()).startsWith("form_");
        assertThat(result.getPdfFileName()).endsWith(".pdf");
        assertThat(result.getLocalDestination()).isEqualTo("/local/form.pdf");
        assertThat(result.getS3Destination()).isEqualTo("s3://bucket/form.pdf");
    }

    @Test
    void generate_callsCsvReader_withConfiguredPath() {
        stubCollaborators();

        formGenerationService.generate(TEST_USERNAME);

        verify(csvReaderService).readCsv(TEST_CSV_PATH);
    }

    @Test
    void generate_callsPdfGenerator_withParsedRecords() {
        List<FormRecord> records = List.of(new FormRecord(Map.of("key", "val")));
        when(csvReaderService.readCsv(anyString())).thenReturn(records);
        when(pdfGeneratorService.generate(records)).thenReturn("bytes".getBytes());
        when(localFileStore.save(anyString(), any())).thenReturn("/local/out.pdf");
        when(s3FileStore.save(anyString(), any())).thenReturn("s3://b/out.pdf");

        formGenerationService.generate(TEST_USERNAME);

        verify(pdfGeneratorService).generate(records);
    }

    @Test
    void generate_savesToBothStores() {
        stubCollaborators();

        formGenerationService.generate(TEST_USERNAME);

        verify(localFileStore).save(anyString(), any());
        verify(s3FileStore).save(anyString(), any());
    }

    @Test
    void generate_logsTransferSpeed() {
        stubCollaborators();

        formGenerationService.generate(TEST_USERNAME);

        verify(transferSpeedLogger).logComparison(anyString(), anyLong(), anyLong());
    }

    @Test
    void generate_enqueuesAuditEvent() {
        stubCollaborators();

        formGenerationService.generate(TEST_USERNAME);

        verify(auditService).recordAsync(
                eq(TEST_USERNAME),
                eq(TEST_CSV_PATH),
                anyString(),
                anyString()
        );
    }

    @Test
    void generate_csvReaderThrows_propagatesException() {
        when(csvReaderService.readCsv(anyString()))
                .thenThrow(new RuntimeException("CSV read failed"));

        assertThatThrownBy(() -> formGenerationService.generate(TEST_USERNAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV read failed");
    }

    @Test
    void generate_pdfGeneratorThrows_doesNotCallFileStores() {
        when(csvReaderService.readCsv(anyString())).thenReturn(List.of());
        when(pdfGeneratorService.generate(any()))
                .thenThrow(new RuntimeException("PDF failed"));

        assertThatThrownBy(() -> formGenerationService.generate(TEST_USERNAME))
                .isInstanceOf(RuntimeException.class);

        verify(localFileStore, never()).save(anyString(), any());
        verify(s3FileStore, never()).save(anyString(), any());
    }

    @Test
    void generate_transferTimesAreNonNegative() {
        stubCollaborators();

        GenerationResult result = formGenerationService.generate(TEST_USERNAME);

        assertThat(result.getLocalTransferMillis()).isGreaterThanOrEqualTo(0);
        assertThat(result.getS3TransferMillis()).isGreaterThanOrEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void stubCollaborators() {
        when(csvReaderService.readCsv(anyString()))
                .thenReturn(List.of(new FormRecord(Map.of("k", "v"))));
        when(pdfGeneratorService.generate(any()))
                .thenReturn("%PDF".getBytes());
        when(localFileStore.save(anyString(), any()))
                .thenReturn("/local/out.pdf");
        when(s3FileStore.save(anyString(), any()))
                .thenReturn("s3://bucket/out.pdf");
    }
}
