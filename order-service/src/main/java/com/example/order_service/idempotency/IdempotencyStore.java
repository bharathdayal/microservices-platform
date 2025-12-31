package com.example.order_service.idempotency;

import com.example.order_service.domain.Order;
import com.example.order_service.repo.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyStore {

    private final OrderRepository orderRepository;

    public IdempotencyStore(OrderRepository orderRepository) {
        this.orderRepository=orderRepository;
    }

    public Optional<Order> findExisting(String key) {
        return orderRepository.findByIdempotencyKey(key);
    }
}
