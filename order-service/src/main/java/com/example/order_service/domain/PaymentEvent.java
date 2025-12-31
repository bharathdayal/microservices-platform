package com.example.order_service.domain;

import java.util.UUID;

public record PaymentEvent(UUID orderId,
                           String status) {
}
