package com.robiulsunyemon.profile_service.profile.config;

import lombok.Data;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RabbitMQConfig {

    @Value("${rabbitmq.messaging.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.messaging.queue}")
    private String queueName;

    @Value("${rabbitmq.messaging.rollback-queue}")
    private String rollBackQueue;

    @Value("${rabbitmq.messaging.kyc-post-queue}")
    private String kycPostQueue;

    @Value("${rabbitmq.messaging.kyc-post-exchange}")
    private String kycPostExchange;

    @Value("${rabbitmq.messaging.kyc-post-routing-key}")
    private String kycPostRoutingKey;

    @Value("${rabbitmq.messaging.routing-key-rollback}")
    private String rollBackRouting;

    @Value("${rabbitmq.messaging.kyc-result-exchange}")
    private String kycResultExchange;

    @Value("${rabbitmq.messaging.kyc-result-queue}")
    private String kycResultQueue;

    @Value("${rabbitmq.messaging.kyc-result-routing-key}")
    private String kycResultRoutingKey;



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
    public Queue profilequeue()  { return new Queue(queueName, true); }

    @Bean
    public Queue kycPostQueue()  { return new Queue(kycPostQueue, true); }

    @Bean
    public Queue rollBackQueue() { return new Queue(rollBackQueue, true); }

    @Bean
    public DirectExchange exchange()       { return new DirectExchange(exchangeName); }

    @Bean
    public DirectExchange kycPostExchange() { return new DirectExchange(kycPostExchange); }

    @Bean
    public Binding kycPostBinding() {
        return BindingBuilder.bind(kycPostQueue()).to(kycPostExchange()).with(kycPostRoutingKey);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(rollBackQueue()).to(exchange()).with(rollBackRouting);
    }

    @Bean
    public Queue kycResultQueue() {
        return new Queue(kycResultQueue, true);  // durable=true
    }

    @Bean
    public DirectExchange kycResultExchange() {
        return new DirectExchange(kycResultExchange);
    }

    @Bean
    public Binding kycResultBinding() {
        return BindingBuilder
                .bind(kycResultQueue())
                .to(kycResultExchange())
                .with(kycResultRoutingKey);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}