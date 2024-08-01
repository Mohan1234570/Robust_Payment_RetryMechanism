package com.krish.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krish.entity.Payment;

public interface PaymentRepo extends JpaRepository<Payment, Integer>{

	Optional<Payment> findByTransactionId(String transactionId);
}
