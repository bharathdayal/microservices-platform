package com.example.order_service.service;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OutboxEvent;
import com.example.order_service.repo.OrderRepository;
import com.example.order_service.repo.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public OrderService(OrderRepository orderRepository,OutboxRepository outboxRepository) {
        this.orderRepository=orderRepository;
        this.outboxRepository=outboxRepository;
    }

    @Transactional
    public UUID createOrder(String customerId,String idemKey) {

        // First-level idempotency (fast path)
        var existing = orderRepository.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        try {
            UUID orderId = UUID.randomUUID();

            Order order =
                    new Order(orderId, customerId, idemKey);
            orderRepository.save(order);

            OutboxEvent event =
                    new OutboxEvent(
                            orderId,
                            "ORDER_CREATED",
                            buildPayload(orderId, customerId)
                    );

            outboxRepository.save(event);

            return orderId;

        } catch (DataIntegrityViolationException ex) {
            // Second-level idempotency (race-safe)
            return orderRepository.findByIdempotencyKey(idemKey)
                    .map(Order::getId)
                    .orElseThrow();
        }


    }

    private String buildPayload(UUID orderId, String customerId) {
        return """
            {"orderId":"%s","customerId":"%s"}
        """.formatted(orderId, customerId);
    }
}
