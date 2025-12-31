package com.example.payment_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PaymentEventPublisher {
    private final KafkaTemplate<Object, Object> kafka;
    private final String completed;
    private final String failed;

    public PaymentEventPublisher(
            KafkaTemplate<Object, Object> kafka,
            @Value("${payment.topic.completed}") String completed,
            @Value("${payment.topic.failed}") String failed
    ) {
        this.kafka = kafka;
        this.completed = completed;
        this.failed = failed;
    }

    public void publishCompleted(UUID orderId) {
        kafka.send(completed, orderId.toString(),
                Map.of("orderId", orderId, "status", "SUCCESS"));
    }

    public void publishFailed(UUID orderId) {
        kafka.send(failed, orderId.toString(),
                Map.of("orderId", orderId, "status", "FAILED"));
    }
}
