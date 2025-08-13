package com.krish.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Setter
@Getter
public class PaymentRequest {

    @NotNull
    @Positive
    private Long amount; // in smallest currency unit

    @NotBlank
    private String currency;

    @NotBlank
    private String orderId;

    @NotBlank
    private String paymentMethod;

    @Email
    @NotBlank
    private String customerEmail;

    // getters and setters
}
