package com.krish.utils;

import com.krish.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentRetryProducer {

    private final RabbitTemplate rabbitTemplate;

    public PaymentRetryProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void scheduleRetry(PaymentRetryMessage msg, long delayMillis) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.RETRY_DELAY_ROUTING,
                msg,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );
    }
}
