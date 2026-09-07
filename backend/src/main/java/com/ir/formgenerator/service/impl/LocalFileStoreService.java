package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.service.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service("localFileStoreService")
public class LocalFileStoreService implements FileStoreService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStoreService.class);

    private final Path outputDirectory;

    public LocalFileStoreService(@Value("${app.local.output-dir}") String outputDir) {
        this.outputDirectory = Paths.get(outputDir);
        try {
            Files.createDirectories(this.outputDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create local output directory: " + outputDir, e);
        }
    }

    @Override
    public String save(String fileName, byte[] fileContent) {
        Path destination = outputDirectory.resolve(fileName);
        try {
            Files.write(destination, fileContent, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved file locally: {}", destination.toAbsolutePath());
            return destination.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to save file locally: {}", fileName, e);
            throw new RuntimeException("Could not save file locally: " + fileName, e);
        }
    }

    @Override
    public List<String> listFiles() {
        try (Stream<Path> paths = Files.list(outputDirectory)) {
            return paths
                    .filter(p -> p.toString().endsWith(".pdf"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list local files", e);
            return Collections.emptyList();
        }
    }
}
