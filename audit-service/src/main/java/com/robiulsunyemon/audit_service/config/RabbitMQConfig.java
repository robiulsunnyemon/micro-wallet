package com.robiulsunyemon.audit_service.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.messaging.queue}")
    private String queueName;

    @Value("${rabbitmq.messaging.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.messaging.routing-key}")
    private String routingKey;

    @Bean
    public Queue auditQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}