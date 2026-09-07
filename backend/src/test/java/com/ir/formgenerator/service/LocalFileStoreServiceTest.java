package com.ir.formgenerator.service;

import com.ir.formgenerator.service.impl.LocalFileStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LocalFileStoreServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStoreService localFileStoreService;

    @BeforeEach
    void setUp() {
        localFileStoreService = new LocalFileStoreService(tempDir.toString());
    }

    @Test
    void save_validContent_returnsAbsolutePath() {
        byte[] content = "PDF content".getBytes();

        String destination = localFileStoreService.save("test.pdf", content);

        assertThat(destination).endsWith("test.pdf");
        assertThat(destination).contains(tempDir.toString());
    }

    @Test
    void save_validContent_writesFileToDisk() {
        byte[] content = "%PDF-test".getBytes();

        localFileStoreService.save("output.pdf", content);

        Path saved = tempDir.resolve("output.pdf");
        assertThat(saved).exists();
    }

    @Test
    void save_validContent_fileContainsCorrectBytes() throws Exception {
        byte[] content = "Hello PDF".getBytes();

        localFileStoreService.save("data.pdf", content);

        byte[] read = Files.readAllBytes(tempDir.resolve("data.pdf"));
        assertThat(read).isEqualTo(content);
    }

    @Test
    void listFiles_noFiles_returnsEmptyList() {
        List<String> files = localFileStoreService.listFiles();

        assertThat(files).isEmpty();
    }

    @Test
    void listFiles_afterSave_containsSavedFileName() {
        localFileStoreService.save("form_001.pdf", "content".getBytes());

        List<String> files = localFileStoreService.listFiles();

        assertThat(files).contains("form_001.pdf");
    }

    @Test
    void listFiles_onlyReturnsPdfFiles() throws Exception {
        localFileStoreService.save("report.pdf", "pdf".getBytes());
        // Write a non-PDF file directly to the temp dir
        Files.writeString(tempDir.resolve("notes.txt"), "not a pdf");

        List<String> files = localFileStoreService.listFiles();

        assertThat(files).containsExactly("report.pdf");
        assertThat(files).doesNotContain("notes.txt");
    }

    @Test
    void listFiles_multipleFiles_returnsSortedList() {
        localFileStoreService.save("form_c.pdf", "c".getBytes());
        localFileStoreService.save("form_a.pdf", "a".getBytes());
        localFileStoreService.save("form_b.pdf", "b".getBytes());

        List<String> files = localFileStoreService.listFiles();

        assertThat(files).containsExactly("form_a.pdf", "form_b.pdf", "form_c.pdf");
    }

    @Test
    void save_overwritesExistingFile() throws Exception {
        localFileStoreService.save("existing.pdf", "original".getBytes());
        localFileStoreService.save("existing.pdf", "updated".getBytes());

        byte[] content = Files.readAllBytes(tempDir.resolve("existing.pdf"));
        assertThat(new String(content)).isEqualTo("updated");
    }
}
