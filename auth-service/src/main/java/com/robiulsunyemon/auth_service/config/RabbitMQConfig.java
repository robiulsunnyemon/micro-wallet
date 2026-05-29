package com.robiulsunyemon.auth_service.config;
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

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.topic_exchange}")
    private String topicExchangeName;


    @Value("${rabbitmq.otp_queue}")
    private String otpQueue;

    @Value("${rabbitmq.wallet_queue}")
    private String walletQueue;


    @Value("${rabbitmq.profile_queue}")
    private String profileQueue;

    @Value("${rabbitmq.routing-key-otp}")
    private String routingKeyOtp;


    @Value("${rabbitmq.routing-key-topic}")
    private String routingKeyTopic;

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchangeName);
    }


    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    public Queue otpQueue(){
        return new Queue(otpQueue,true);
    }

    @Bean
    public Queue walletQueue(){
        return new Queue(walletQueue,true);
    }

    @Bean
    public Queue profileQueue(){
        return new Queue(profileQueue,true);
    }



    @Bean
    public Binding otpBinding(){
        return BindingBuilder.bind(otpQueue()).to(exchange()).with(routingKeyOtp);

    }

    @Bean
    public Binding walletBinding(Queue walletQueue,TopicExchange exchange){
        return BindingBuilder.bind(walletQueue).to(exchange).with(routingKeyTopic);
    }

    @Bean
    public Binding profileBinding(Queue profileQueue,TopicExchange exchange){
        return BindingBuilder.bind(profileQueue).to(exchange).with(routingKeyTopic);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

}