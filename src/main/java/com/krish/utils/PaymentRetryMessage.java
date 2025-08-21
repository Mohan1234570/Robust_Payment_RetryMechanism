package com.krish.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentRetryMessage {
    private String paymentId;
    private int attemptNo;
}

