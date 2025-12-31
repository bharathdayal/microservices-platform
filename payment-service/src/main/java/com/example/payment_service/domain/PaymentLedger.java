package com.example.payment_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="payment_ledger",uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
@Data
@AllArgsConstructor
public class PaymentLedger {

    @Id
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentLedger() {}

    public PaymentLedger(UUID orderId, PaymentStatus status) {
        this.paymentId = UUID.randomUUID();
        this.orderId = orderId;
        this.status = status;
        this.createdAt = Instant.now();
    }
}
