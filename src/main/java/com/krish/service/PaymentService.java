package com.krish.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krish.entity.Payment;
import com.krish.entity.Transaction;
import com.krish.repo.PaymentRepo;
import com.krish.repo.TransactionRepo;

@Service
public class PaymentService {

	 @Autowired
	    private PaymentRepo paymentRepository;

	    @Autowired
	    private TransactionRepo transactionRepository;

	    private static final int MAX_RETRIES = 3;

	    /**
	     * Process a payment and handle retries if needed.
	     *
	     * @param payment the payment to process
	     * @return the updated payment
	     */
	    @Transactional
	    public Payment processPayment(Payment payment) {
	        payment.setTimestamp(LocalDateTime.now());
	        payment.setStatus("PENDING");
	        Payment savedPayment = paymentRepository.save(payment);

	        boolean paymentSuccessful = attemptPayment(savedPayment);

	        if (!paymentSuccessful) {
	            retryPayment(savedPayment);
	        }

	        return savedPayment;
	    }

	    /**
	     * Attempt to process the payment.
	     *
	     * @param payment the payment to process
	     * @return true if successful, false otherwise
	     */
	    private boolean attemptPayment(Payment payment) {
	        // Simulate payment processing logic
	        boolean success = Math.random() > 0.5; // Random success/failure
	        payment.setStatus(success ? "SUCCESS" : "FAILED");
	        paymentRepository.save(payment);
	        return success;
	    }

	    /**
	     * Retry the payment in case of failure.
	     *
	     * @param payment the payment to retry
	     */
	    private void retryPayment(Payment payment) {
	        for (int i = 1; i <= MAX_RETRIES; i++) {
	            Transaction transaction = new Transaction();
	            transaction.setPayment(payment);
	            transaction.setRetryTimestamp(LocalDateTime.now());
	            transaction.setRetryCount(i);
	            transactionRepository.save(transaction);

	            boolean retrySuccess = attemptPayment(payment);
	            if (retrySuccess) {
	                break;
	            }
	        }
	    }

	    
	    public List<Payment> getAllPayments() {
	        return paymentRepository.findAll();
	    }
}
