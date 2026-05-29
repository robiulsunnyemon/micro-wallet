package com.robiulsunyemon.wallet_service.wallet.config;
import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class RabbitMQConfig {
    @Value("${rabbitmq.queue}")
    private String QUEUE_NAME;
    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;
    @Value("${rabbitmq.routing-key}")
    private String ROUTING_KEY;


    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }


    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }


    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

}