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
    public DirectExchange paymentExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE).build();
    }

    @Bean
    public Queue retryDelayQueue() {
        // messages published here expire and dead-letter to RETRY_QUEUE
        return QueueBuilder.durable(RETRY_DELAY_QUEUE)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", EXCHANGE,
                        "x-dead-letter-routing-key", RETRY_ROUTING
                ))
                .build();
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue()).to(paymentExchange()).with(RETRY_ROUTING);
    }

    @Bean
    public Binding retryDelayBinding() {
        return BindingBuilder.bind(retryDelayQueue()).to(paymentExchange()).with(RETRY_DELAY_ROUTING);
    }
}
