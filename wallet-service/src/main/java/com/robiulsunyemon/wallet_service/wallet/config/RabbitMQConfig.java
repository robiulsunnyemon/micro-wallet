package com.robiulsunyemon.wallet_service.wallet.config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RabbitMQConfig {
    @Value("${rabbitmq.queue}")
    private String QUEUE_NAME;
    @Value("${rabbitmq.profile-queue}")
    private String PROFILE_QUEUE_NAME;
    @Value("${rabbitmq.rollback-queue}")
    private String ROLLBACK_QUEUE_NAME;

    @Value("${rabbitmq.rollback-queue-profile}")
    private String ROLLBACK_PROFILE_QUEUE_NAME;
    @Value("${rabbitmq.exchange}")
    private String EXCHANGE_NAME;
    @Value("${rabbitmq.routing-key}")
    private String ROUTING_KEY;

    @Value("${rabbitmq.rollback-routing-key}")
    private String ROLLBACK_ROUTING_KEY;

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Queue profileQueue() {
        return new Queue(PROFILE_QUEUE_NAME, true);
    }

    @Bean
    public Queue rollbackQueue() {
        return new Queue(ROLLBACK_QUEUE_NAME, true);
    }


    @Bean
    public Queue rollbackProfileQueue() {
        return new Queue(ROLLBACK_PROFILE_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }


    @Bean
    public Binding binding() {
        return BindingBuilder.bind(profileQueue()).to(exchange()).with(ROUTING_KEY);
    }



    @Bean
    public Binding rollbackBinding() {
        return BindingBuilder.bind(rollbackQueue()).to(exchange()).with(ROLLBACK_ROUTING_KEY);
    }



    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

}