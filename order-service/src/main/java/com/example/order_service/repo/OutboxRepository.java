package com.example.order_service.repo;

import com.example.order_service.domain.OutboxEvent;
import com.example.order_service.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(OutboxStatus status);
}
