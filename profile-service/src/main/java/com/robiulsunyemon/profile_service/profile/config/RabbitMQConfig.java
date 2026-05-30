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

    @Value("${rabbitmq.exchange}")
    private String exchangeName;


    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.rollback-queue}")
    private String rollBackQueue;


    @Value("${rabbitmq.routing-key-rollback}")
    private String rollBackRouting;


    @Bean
    public Queue profilequeue(){
        return new Queue(queueName,true);
    }


    @Bean
    public Queue rollBackQueue(){
        return new Queue(rollBackQueue,true);
    }

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(rollBackQueue()).to(exchange()).with(rollBackRouting);
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
