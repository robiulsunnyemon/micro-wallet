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

    @Value("${rabbitmq.otp-queue}")
    private String otpQueue;

    @Value("${rabbitmq.rollback-queue}")
    private String rollbackWalletQueue;

    @Value("${rabbitmq.wallet-queue}")
    private String walletQueue;

    @Value("${rabbitmq.routing-key-otp}")
    private String routingKeyOtp;


    @Value("${rabbitmq.routing-key-wallet}")
    private String routingKeyWallet;

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(exchangeName);
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
    public Queue rollbackWalletQueue(){
        return new Queue(rollbackWalletQueue,true);
    }

    @Bean
    public Binding otpBinding(){
        return BindingBuilder.bind(otpQueue()).to(exchange()).with(routingKeyOtp);

    }

    @Bean
    public Binding walletBinding(){
        return BindingBuilder.bind(walletQueue()).to(exchange()).with(routingKeyWallet);
    }


    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

}