package com.krish.utils;

import com.krish.entity.Payment;
import com.krish.repo.PaymentRepo;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class PaymentRetryWorker {

    private final PaymentRepo paymentRepo;

    @Value("${app.stripe.success-url}")
    private String successUrl;
    @Value("${app.stripe.cancel-url}")
    private String cancelUrl;
    @Value("${app.retry.max-attempts:6}")
    private int maxAttempts;

    public PaymentRetryWorker(PaymentRepo paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @RabbitListener(queues = "payment.retry.queue")
    @Transactional
    public void handleRetry(PaymentRetryMessage msg) {
        Optional<Payment> maybe = paymentRepo.findById(msg.getPaymentId());
        if (maybe.isEmpty()) return;

        Payment p = maybe.get();
        if (p.isTerminal()) return;
        if (p.getRetryCount() >= maxAttempts) {
            p.setStatus("FAILED");
            paymentRepo.save(p);
            return;
        }

        // For Stripe Checkout: create a new session again
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(p.getCurrency())
                                                    .setUnitAmount(p.getAmount() * 100) // cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData
                                                                    .builder()
                                                                    .setName("Order " + p.getOrderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            p.setGatewayPaymentId(session.getId());
            p.setStatus("RETRYING");              // still pending until webhook completes
            p.setLastRetryAt(Instant.now());
            p.setNextRetryAt(null);               // this retry already happened
            paymentRepo.save(p);

            // In a real app, email/SMS the new checkout URL to the customer:
            // session.getUrl()

        } catch (StripeException e) {
            // If creation itself fails, schedule another retry (or mark failed if too many)
            p.setRetryCount(p.getRetryCount() + 1);
            paymentRepo.save(p);
            // Let webhook controller or a scheduler compute & schedule next retry
        }
    }
}