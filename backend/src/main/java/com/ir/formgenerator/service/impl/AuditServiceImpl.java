package com.ir.formgenerator.service.impl;

import com.ir.formgenerator.model.AuditLog;
import com.ir.formgenerator.queue.AuditMessage;
import com.ir.formgenerator.queue.AuditProducer;
import com.ir.formgenerator.repository.AuditLogRepository;
import com.ir.formgenerator.service.AuditService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuditServiceImpl implements AuditService {

    private final AuditProducer auditProducer;
    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditProducer auditProducer,
                            AuditLogRepository auditLogRepository) {
        this.auditProducer = auditProducer;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void recordAsync(String username, String csvFileInput,
                            String pdfFileOutput, String fileDestination) {
        AuditMessage message = new AuditMessage(username, csvFileInput,
                pdfFileOutput, fileDestination);
        auditProducer.publish(message);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
