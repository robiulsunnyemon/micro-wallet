package com.robiulsunyemon.transaction_service.transaction.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RabbitMQConfig {

    @Value("${rabbitmq.messaging.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.messaging.routing-key}")
    private String routingKey;

    @Value("${rabbitmq.messaging.rollback-queue}")
    private String rollbackQueueName;

    @Value("${rabbitmq.messaging.rollback-routing-key}")
    private String rollbackRoutingKey;

    @Value("${rabbitmq.messaging.audit-exchange}")
    private String auditExchange;

    @Value("${rabbitmq.messaging.audit-queue}")
    private String auditQueue;

    @Value("${rabbitmq.messaging.audit-routing-key}")
    private String auditRoutingKey;

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(auditExchange);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(auditQueue, true);
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
                .bind(auditQueue())
                .to(auditExchange())
                .with(auditRoutingKey);
    }

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue transactionRollbackQueue() {
        return new Queue(rollbackQueueName, true);
    }

    @Bean
    public Binding transactionRollbackBinding() {
        return BindingBuilder
                .bind(transactionRollbackQueue())
                .to(transactionExchange())
                .with(rollbackRoutingKey);
    }
    
    // Binding is not strictly necessary for rollback queue here since wallet-service binds it, but good for safety.

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
