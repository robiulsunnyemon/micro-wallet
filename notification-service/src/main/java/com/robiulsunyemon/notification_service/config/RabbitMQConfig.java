package com.robiulsunyemon.notification_service.config;
import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;


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
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}