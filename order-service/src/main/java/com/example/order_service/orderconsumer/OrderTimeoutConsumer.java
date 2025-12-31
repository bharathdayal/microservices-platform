package com.example.order_service.orderconsumer;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderStatus;
import com.example.order_service.repo.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class OrderTimeoutConsumer {

    private final OrderRepository orderRepo;

    public OrderTimeoutConsumer(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @KafkaListener(
            topics = "order.payment.timeout",
            groupId = "order-service"
    )
    @Transactional
    public void handleTimeout(Map<String, Object> event) {

        UUID orderId =
                UUID.fromString(event.get("orderId").toString());

        Order order = orderRepo.findById(orderId)
                .orElseThrow();

        // Idempotency
        if (order.getStatus() == OrderStatus.PAYMENT_TIMEOUT ||
                order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.cancel();
    }
}
