package com.example.order_service.domain;

public enum OrderStatus {

    CREATED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    PAYMENT_TIMEOUT,
    CANCELLED
}
