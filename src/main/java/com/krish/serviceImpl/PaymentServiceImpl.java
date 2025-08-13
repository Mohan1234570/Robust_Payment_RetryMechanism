package com.krish.serviceImpl;

import com.krish.dto.PaymentRequest;
import com.krish.dto.PaymentResponse;
import com.krish.entity.Payment;
import com.krish.repo.PaymentRepo;
import com.krish.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepository;

    public PaymentServiceImpl(PaymentRepo paymentRepository) {
        this.paymentRepository = paymentRepository;
        Stripe.apiKey = System.getenv("STRIPE_API_KEY"); // ✅ Read from env var
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Idempotency check
        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(p -> { throw new RuntimeException("Duplicate payment for order"); });

        // Create payment entity
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setCustomerEmail(request.getCustomerEmail());
        payment.setStatus("CREATED");
        payment.setGateway(determineGateway(request.getPaymentMethod()));

        // Save to DB first
        payment = paymentRepository.save(payment);

        // Call Stripe Sandbox API
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:8080/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency())
                                                    .setUnitAmount(request.getAmount().longValue() * 100) // cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order " + request.getOrderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // Save Stripe session ID
            payment.setGatewayPaymentId(session.getId());
            paymentRepository.save(payment);

            return new PaymentResponse(payment.getPaymentId(), payment.getStatus(), session.getUrl());

        } catch (StripeException e) {
            throw new RuntimeException("Stripe payment initiation failed", e);
        }
    }

    private String determineGateway(String paymentMethod) {
        return switch (paymentMethod.toUpperCase()) {
            case "UPI" -> "Razorpay";
            case "CARD" -> "Stripe";
            default -> "ManualGateway";
        };
    }
}
