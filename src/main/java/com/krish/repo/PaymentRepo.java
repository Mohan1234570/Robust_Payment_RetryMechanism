package com.krish.repo;

import com.krish.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment, String> {
	Optional<Payment> findByOrderId(String orderId);
	Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

}
