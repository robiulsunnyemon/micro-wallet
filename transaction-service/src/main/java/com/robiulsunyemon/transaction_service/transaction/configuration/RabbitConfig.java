package com.robiulsunyemon.transaction_service.transaction.configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.transection-queue}")
    private String transactionQueue;

    @Value("${rabbitmq.rollback-transection-queue}")
    private String rollbackTransactionQueue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Value("${rabbitmq.rollback-queue}")
    private String rollbackRoutingKey;


    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(exchange);
    }


    @Bean
    public Queue transactionQueue() {
        return new Queue(transactionQueue, true);
    }


    @Bean
    public Queue rollbackTransactionQueue() {
        return new Queue(rollbackTransactionQueue, true);
    }


    @Bean
    public Binding transactionBinding(Queue transactionQueue, TopicExchange transactionExchange) {
        return BindingBuilder
                .bind(transactionQueue)
                .to(transactionExchange)
                .with(routingKey);
    }


    @Bean
    public Binding rollbackTransactionBinding(Queue rollbackTransactionQueue, TopicExchange transactionExchange) {
        return BindingBuilder
                .bind(rollbackTransactionQueue)
                .to(transactionExchange)
                .with(rollbackRoutingKey);
    }



    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}