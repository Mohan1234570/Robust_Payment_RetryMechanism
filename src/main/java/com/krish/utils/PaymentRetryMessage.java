package com.krish.utils;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Setter
@Getter
public class PaymentRetryMessage {
    private String paymentId;
    private int attemptNo;
}

