package com.ir.formgenerator.controller;

import com.ir.formgenerator.dto.AuditLogDto;
import com.ir.formgenerator.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }


    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
        List<AuditLogDto> logs = auditService.findAll()
                .stream()
                .map(AuditLogDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
}
