package com.example.order_service.paymentconsumer;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderStatus;
import com.example.order_service.repo.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;


@Component
public class PaymentEventConsumer {

    private final OrderRepository repository;


    public PaymentEventConsumer(OrderRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = {
                    "payment.completed",
                    "payment.failed"
            },
            groupId = "order-microservice"
    )
    @Transactional
    public void handlePaymentEvent(Map<String, Object> event) {

        UUID orderId = UUID.fromString(event.get("orderId").toString());
        String status = event.get("status").toString();

        Order order = repository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalStateException("Order not found: " + orderId)
                );

        // ---- Idempotency guard (important)
        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }

        if ("SUCCESS".equals(status)) {
            order.markPaid();
        } else {
            order.markPaymentFailed();
        }

        repository.save(order);
    }
}
