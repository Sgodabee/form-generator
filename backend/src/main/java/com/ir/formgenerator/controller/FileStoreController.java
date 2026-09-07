package com.ir.formgenerator.controller;

import com.ir.formgenerator.service.FileStoreService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/files")
public class FileStoreController {

    private final FileStoreService localFileStore;

    public FileStoreController(
            @Qualifier("localFileStoreService") FileStoreService localFileStore) {
        this.localFileStore = localFileStore;
    }

    @GetMapping
    public ResponseEntity<List<String>> listFiles() {
        return ResponseEntity.ok(localFileStore.listFiles());
    }
}
