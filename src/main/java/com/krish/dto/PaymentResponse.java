package com.krish.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentResponse {
    private String paymentId;
    private String status;
    private String redirectUrl;

    public PaymentResponse(String paymentId, String status, String redirectUrl) {
        this.paymentId = paymentId;
        this.status = status;
        this.redirectUrl = redirectUrl;
    }

    // getters
    
}
