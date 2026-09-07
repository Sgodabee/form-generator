package com.ir.formgenerator.queue;

import com.ir.formgenerator.config.RabbitMQConfig;
import com.ir.formgenerator.model.AuditLog;
import com.ir.formgenerator.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    private final AuditLogRepository auditLogRepository;

    public AuditConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void consume(AuditMessage message) {
        log.info("Consuming audit message: {}", message);

        AuditLog auditLog = new AuditLog(
                message.getUsername(),
                message.getCsvFileInput(),
                message.getPdfFileOutput(),
                message.getFileDestination(),
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
        log.info("Audit log persisted for user: {}", message.getUsername());
    }
}
