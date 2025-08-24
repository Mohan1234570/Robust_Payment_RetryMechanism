package com.krish.records;

import java.io.Serializable;

public record PaymentRetryMessage(String paymentId, int retryCount) implements Serializable { }
