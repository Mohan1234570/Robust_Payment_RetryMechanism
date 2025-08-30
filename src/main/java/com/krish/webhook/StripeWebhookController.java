package com.krish.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krish.entity.Payment;
import com.krish.repo.PaymentRepo;
import com.krish.utils.PaymentRetryMessage;
import com.krish.utils.PaymentRetryProducer;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final PaymentRepo paymentRepo;
    private final PaymentRetryProducer retryProducer;
    private final ObjectMapper objectMapper;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.retry.max-attempts:6}")
    private int maxAttempts;

    public StripeWebhookController(PaymentRepo paymentRepo,
                                   PaymentRetryProducer retryProducer,
                                   ObjectMapper objectMapper) {
        this.paymentRepo = paymentRepo;
        this.retryProducer = retryProducer;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/stripe")
    @Transactional
    public ResponseEntity<String> handleStripe(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String sigHeader) {
        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("✅ Received Stripe event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (session == null) break;

                log.info("🎉 Checkout completed for session: {}", session.getId());

                paymentRepo.findByGatewayPaymentId(session.getId()).ifPresentOrElse(payment -> {
                    if (!payment.isTerminal()) {
                        payment.setStatus("SUCCESS");
                        payment.setLastRetryAt(Instant.now());
                        paymentRepo.save(payment);
                        log.info("✅ Payment {} marked as SUCCESS", payment.getPaymentId());
                    } else {
                        log.info("ℹ️ Payment {} is already terminal, skipping", payment.getPaymentId());
                    }
                }, () -> log.warn("⚠️ No payment found for session {}", session.getId()));
            }

            case "checkout.session.expired" -> {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (session == null) break;

                log.warn("⌛ Checkout expired for session: {}", session.getId());

                paymentRepo.findByGatewayPaymentId(session.getId()).ifPresentOrElse(payment -> {
                    if (payment.isTerminal()) {
                        log.info("ℹ️ Payment {} already terminal, skipping retry", payment.getPaymentId());
                        return;
                    }
                    payment.setStatus("RETRYING");
                    payment.setRetryCount(payment.getRetryCount() + 1);
                    payment.setLastRetryAt(Instant.now());

                    long delay = computeBackoffMillis(payment.getRetryCount());
                    payment.setNextRetryAt(Instant.now().plusMillis(delay));
                    paymentRepo.save(payment);

                    retryProducer.scheduleRetry(
                            new PaymentRetryMessage(payment.getPaymentId(), payment.getRetryCount()),
                            delay
                    );

                    log.info("🔁 Payment {} scheduled for retry #{} after {} ms",
                            payment.getPaymentId(), payment.getRetryCount(), delay);
                }, () -> log.warn("⚠️ No payment found for expired session {}", session.getId()));
            }

            default -> log.debug("ℹ️ Ignored Stripe event: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }

    private long computeBackoffMillis(int attempt) {
        return switch (attempt) {
            case 1 -> Duration.ofSeconds(30).toMillis();
            case 2 -> Duration.ofMinutes(2).toMillis();
            case 3 -> Duration.ofMinutes(10).toMillis();
            case 4 -> Duration.ofHours(1).toMillis();
            default -> Duration.ofHours(6).toMillis();
        };
    }
}
