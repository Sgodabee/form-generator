package com.ir.formgenerator.service;

import com.ir.formgenerator.model.FormRecord;
import com.ir.formgenerator.service.impl.PdfGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class PdfGeneratorServiceImplTest {

    private PdfGeneratorServiceImpl pdfGeneratorService;

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PdfGeneratorServiceImpl();
    }

    @Test
    void generate_withRecords_returnsByteArray() {
        List<FormRecord> records = List.of(
                new FormRecord(Map.of("name", "Vutivi", "age", "30")),
                new FormRecord(Map.of("name", "Ntwani", "age", "25"))
        );

        byte[] pdf = pdfGeneratorService.generate(records);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
    }

    @Test
    void generate_withRecords_producesValidPdfHeader() {
        List<FormRecord> records = List.of(
                new FormRecord(Map.of("field", "value"))
        );

        byte[] pdf = pdfGeneratorService.generate(records);

        // PDF files always start with the magic bytes "%PDF"
        String header = new String(pdf, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void generate_emptyRecordsList_returnsByteArrayWithContent() {
        byte[] pdf = pdfGeneratorService.generate(List.of());

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        // Still a valid PDF despite no records
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generate_singleRecord_doesNotThrow() {
        FormRecord record = new FormRecord(Map.of(
                "first_name", "John",
                "last_name", "Doe",
                "id_number", "8801015009087"
        ));

        assertThatCode(() -> pdfGeneratorService.generate(List.of(record)))
                .doesNotThrowAnyException();
    }

    @Test
    void generate_largeDataSet_completesWithoutError() {
        List<FormRecord> records = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> new FormRecord(Map.of(
                        "id", String.valueOf(i),
                        "name", "User " + i,
                        "value", "Value " + i)))
                .toList();

        byte[] pdf = pdfGeneratorService.generate(records);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
    }
}
