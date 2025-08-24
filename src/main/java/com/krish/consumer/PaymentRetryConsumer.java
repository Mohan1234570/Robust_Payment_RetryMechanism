package com.krish.consumer;

import com.krish.entity.Payment;
import com.krish.repo.PaymentRepo;
import com.krish.utils.PaymentRetryMessage;
import com.krish.service.StripeService;
import com.stripe.model.checkout.Session;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentRetryConsumer {

    private final PaymentRepo paymentRepo;
    private final StripeService stripeService;

    public PaymentRetryConsumer(PaymentRepo paymentRepo, StripeService stripeService) {
        this.paymentRepo = paymentRepo;
        this.stripeService = stripeService;
    }

    @RabbitListener(queues = "payment.retry.main")
    public void processRetry(PaymentRetryMessage message) {
        paymentRepo.findById(message.getPaymentId()).ifPresent(payment -> {
            if (payment.isTerminal()) return; // don’t retry if SUCCESS or FAILED permanently

            try {
                // 1. Create a new Stripe Checkout session
                Session newSession = stripeService.createCheckoutSession(payment);

                // 2. Update DB with new Stripe session details
                payment.setGatewayPaymentId(newSession.getId());
                payment.setCheckoutUrl(newSession.getUrl());
                payment.setStatus("PENDING");
                paymentRepo.save(payment);

                // 3. (Optional) Send user the new checkout URL
                System.out.println("🔁 Retry session created for Payment " + payment.getPaymentId()
                        + " → " + newSession.getUrl());

            } catch (Exception e) {
                e.printStackTrace();
                // Optionally: schedule another retry or mark as FAILED
            }
        });
    }
}
