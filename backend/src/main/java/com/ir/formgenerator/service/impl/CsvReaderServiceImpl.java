package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.model.FormRecord;
import com.ir.formgenerator.service.CsvReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


@Service
public class CsvReaderServiceImpl implements CsvReaderService {

    private static final Logger log = LoggerFactory.getLogger(CsvReaderServiceImpl.class);
    private static final String DELIMITER = ",";

    @Override
    public List<FormRecord> readCsv(String csvFilePath) {
        log.info("Reading CSV file: {}", csvFilePath);

        List<FormRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                log.warn("CSV file is empty or has no header: {}", csvFilePath);
                return records;
            }

            String[] headers = splitLine(headerLine);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = splitLine(line);
                Map<String, String> fields = new LinkedHashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String value = (i < values.length) ? values[i].trim() : "";
                    fields.put(headers[i].trim(), value);
                }

                records.add(new FormRecord(fields));
            }

        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", csvFilePath, e);
            throw new RuntimeException("Could not read CSV file: " + csvFilePath, e);
        }

        log.info("Parsed {} record(s) from {}", records.size(), csvFilePath);
        return records;
    }

    private String[] splitLine(String line) {
        // Basic split — handles plain CSV without quoted commas
        return line.split(DELIMITER, -1);
    }
}
