package com.krish.restcontroller;

package com.payment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krish.entity.Payment;
import com.krish.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

   
    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody Payment payment, Authentication authentication) {
        try {
            String username = authentication.getUsername();
            payment = paymentService.processPaymentForUser(payment, username);
            return new ResponseEntity<>(payment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

   
    @GetMapping("/history")
    public ResponseEntity<List<Payment>> getPaymentHistory(Authentication authentication) {
        String username = authentication.getUsername();
        List<Payment> paymentHistory = paymentService.getPaymentHistory(username);
        return new ResponseEntity<>(paymentHistory, HttpStatus.OK);
    }
}
