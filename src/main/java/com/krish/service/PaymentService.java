package com.krish.service;

import java.time.LocalDateTime;
import java.util.List;

import com.krish.dto.PaymentRequest;
import com.krish.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krish.entity.Payment;
import com.krish.entity.Transaction;
import com.krish.repo.PaymentRepo;
//import com.krish.repo.TransactionRepo;


public interface  PaymentService {
	PaymentResponse createPayment(PaymentRequest request);
}
