package com.example.order_service.repo;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdempotencyKey(String key);

    List<Order> findByStatusAndCreatedAtBefore(
            OrderStatus status,
            Instant cutoff
    );
}
