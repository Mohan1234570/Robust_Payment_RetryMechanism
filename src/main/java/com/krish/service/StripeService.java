package com.krish.service;

import com.krish.entity.Payment;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripeService {

    public StripeService() {
        String stripeApiKey = System.getenv("STRIPE_API_KEY");
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            throw new IllegalStateException("STRIPE_API_KEY environment variable is not set!");
        }
        Stripe.apiKey = stripeApiKey;
    }

    public Session createCheckoutSession(Payment payment) throws Exception {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success?paymentId=" + payment.getPaymentId())
                .setCancelUrl("http://localhost:8080/cancel?paymentId=" + payment.getPaymentId())
                .addAllLineItem(
                        List.of(SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(payment.getCurrency())
                                                .setUnitAmount(payment.getAmount()) // amount in cents/paise
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Retry Payment for Order " + payment.getPaymentId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                        )
                )
                .build();

        return Session.create(params);
    }
}
