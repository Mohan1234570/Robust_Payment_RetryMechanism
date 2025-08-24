package com.krish.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "payments.exchange";
    public static final String RETRY_QUEUE = "payment.retry.queue";
    public static final String RETRY_DELAY_QUEUE = "payment.retry.delay.queue";
    public static final String RETRY_ROUTING = "payment.retry";
    public static final String RETRY_DELAY_ROUTING = "payment.retry.delay";

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange("payment.retry.exchange");
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable("payment.retry.delay")
                .withArgument("x-dead-letter-exchange", "payment.retry.exchange")
                .withArgument("x-dead-letter-routing-key", "retry")
                .build();
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable("payment.retry.main").build();
    }

    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(delayQueue)
                .to(retryExchange)
                .with("delay");
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(retryQueue)
                .to(retryExchange)
                .with("retry");
    }
}
