package com.example.payment_service.consumer;

import com.example.payment_service.repo.PaymentLedgerRepository;
import com.example.payment_service.service.PaymentProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class OrderCreatedConsumer {

    private final PaymentProcessor processor;

    public OrderCreatedConsumer(PaymentProcessor processor) {
        this.processor = processor;
    }


    @KafkaListener(
            topics = "order.created",
            groupId = "payment-microservice",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            Map<String, Object> event,
            Acknowledgment ack
    ) {
        UUID orderId = UUID.fromString(event.get("orderId").toString());

        processor.processPayment(orderId);

        ack.acknowledge();
    }
}
