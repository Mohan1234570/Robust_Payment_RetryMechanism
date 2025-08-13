package com.krish.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String paymentId;

	private String orderId;
	private Long amount;
	private String currency;
	private String paymentMethod;
	private String customerEmail;
	private String status; // CREATED, PROCESSING, SUCCESS, FAILED
	private String gateway;

	// 🔹 This will store the Stripe/Razorpay session or order ID
	private String gatewayPaymentId;

	private Instant createdAt = Instant.now();

	// getters and setters
}