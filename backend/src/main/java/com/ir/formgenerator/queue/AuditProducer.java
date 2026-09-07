package com.ir.formgenerator.queue;

import com.ir.formgenerator.config.RabbitMQConfig;
import org.slf4j.Logger;

import com.ir.formgenerator.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class AuditProducer {

    private static final Logger log = LoggerFactory.getLogger(AuditProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public AuditProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void publish(AuditMessage message) {
        log.info("Publishing audit message to queue: {}", message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUDIT_EXCHANGE,
                RabbitMQConfig.AUDIT_ROUTING_KEY,
                message);
    }
}
