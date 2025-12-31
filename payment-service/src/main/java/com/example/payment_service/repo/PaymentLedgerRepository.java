package com.example.payment_service.repo;

import com.example.payment_service.domain.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, UUID> {

    Optional<PaymentLedger> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);
}
