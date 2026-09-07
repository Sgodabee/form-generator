package com.ir.formgenerator.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String AUDIT_QUEUE    = "audit.queue";
    public static final String AUDIT_EXCHANGE = "audit.exchange";
    public static final String AUDIT_ROUTING_KEY = "audit.key";

    @Bean
    public Queue auditQueue() {

        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange auditExchange) {
        return BindingBuilder.bind(auditQueue)
                .to(auditExchange)
                .with(AUDIT_ROUTING_KEY);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
