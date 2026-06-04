package com.robiulsunyemon.fraud_detection_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.messaging.exchange}")
    private String exchange;

    @Value("${rabbitmq.messaging.fraud-queue}")
    private String fraudQueue;

    @Value("${rabbitmq.messaging.fraud-rollback-queue}")
    private String rollbackFraudQueue;

    @Value("${rabbitmq.messaging.fraud-routing-key}")
    private String fraudRoutingKey;


    @Value("${rabbitmq.messaging.wallet-routing-key}")
    private String walletRoutingKey;

    @Value("${rabbitmq.messaging.rollback-routing-key}")
    private String rollbackRoutingKey;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(exchange);
    }


    @Bean
    public Queue fraudQueue() {
        return new Queue(fraudQueue, true); // durable = true
    }

    @Bean
    public Queue rollbackFraudQueue() {
        return new Queue(rollbackFraudQueue, true); // durable = true
    }


    @Bean
    public Binding rollbackFraudBinding() {
        return BindingBuilder
                .bind(rollbackFraudQueue())
                .to(transactionExchange())
                .with(rollbackRoutingKey);
    }



    @Bean
    public Binding fraudBinding() {
        return BindingBuilder
                .bind(fraudQueue())
                .to(transactionExchange())
                .with(fraudRoutingKey);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}