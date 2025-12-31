package com.example.order_service.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="orders",uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@Data
@AllArgsConstructor
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    protected Order() {}

    public void markPaymentPending() {
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void markPaymentTimeout() {
        this.status = OrderStatus.PAYMENT_TIMEOUT;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public Order(UUID id, String customerId, String idemKey) {
        this.id=id;
        this.createdAt=Instant.now();
        this.idempotencyKey=idemKey;
        this.customerId=customerId;
        this.status=OrderStatus.CREATED;
    }




}
