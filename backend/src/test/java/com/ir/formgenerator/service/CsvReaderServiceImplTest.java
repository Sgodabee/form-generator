package com.ir.formgenerator.service;

import com.ir.formgenerator.model.FormRecord;
import com.ir.formgenerator.service.impl.CsvReaderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CsvReaderServiceImplTest {

    private CsvReaderServiceImpl csvReaderService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvReaderService = new CsvReaderServiceImpl();
    }

    @Test
    void readCsv_validFile_returnsCorrectNumberOfRecords() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv,
                "name,age,city\n" +
                "Vutivi,30,Kempton Park\n" +
                "Bob,25,Johannesburg\n");

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records).hasSize(2);
    }

    @Test
    void readCsv_validFile_mapsHeadersToFieldsCorrectly() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "first_name,last_name\nJohn,Doe\n");

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getField("first_name")).isEqualTo("John");
        assertThat(records.get(0).getField("last_name")).isEqualTo("Doe");
    }

    @Test
    void readCsv_emptyFile_returnsEmptyList() throws IOException {
        Path csv = tempDir.resolve("empty.csv");
        Files.writeString(csv, "");

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records).isEmpty();
    }

    @Test
    void readCsv_headerOnlyFile_returnsEmptyList() throws IOException {
        Path csv = tempDir.resolve("header_only.csv");
        Files.writeString(csv, "name,age,city\n");

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records).isEmpty();
    }

    @Test
    void readCsv_missingField_defaultsToEmptyString() throws IOException {
        Path csv = tempDir.resolve("sparse.csv");
        Files.writeString(csv, "name,age,city\nAlice,30\n");  // city missing

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records.get(0).getField("city")).isEmpty();
    }

    @Test
    void readCsv_nonExistentFile_throwsRuntimeException() {
        assertThatThrownBy(() -> csvReaderService.readCsv("/nonexistent/path/file.csv"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not read CSV file");
    }

    @Test
    void readCsv_multipleRecords_preservesOrder() throws IOException {
        Path csv = tempDir.resolve("ordered.csv");
        Files.writeString(csv, "id\n1\n2\n3\n");

        List<FormRecord> records = csvReaderService.readCsv(csv.toString());

        assertThat(records).hasSize(3);
        assertThat(records.get(0).getField("id")).isEqualTo("1");
        assertThat(records.get(1).getField("id")).isEqualTo("2");
        assertThat(records.get(2).getField("id")).isEqualTo("3");
    }
}
