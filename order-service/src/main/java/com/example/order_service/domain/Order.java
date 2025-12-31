package com.example.order_service.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="orders",uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))

public class Order {



    /* =========================
           Identity
    ========================= */
    @Id
    private UUID id;

    /* =========================
       Business Fields
       ========================= */
    @Column(nullable = false)
    private String customerId;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /* =========================
       JPA Constructor
       ========================= */
    protected Order() {
        // JPA only
    }

    /* =========================
       Factory Method (ONLY entry)
       ========================= */
    public static Order create(
            UUID id,
            String customerId,
            String idempotencyKey
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");

        return new Order(
                id,
                customerId,
                idempotencyKey,
                OrderStatus.CREATED,
                Instant.now()
        );
    }

    public Order(UUID id, String customerId, String idemKey) {
        this.id=id;
        this.createdAt=Instant.now();
        this.idempotencyKey=idemKey;
        this.customerId=customerId;
        this.status=OrderStatus.CREATED;
    }

    /* =========================
       Private Constructor
       ========================= */
    private Order(
            UUID id,
            String customerId,
            String idempotencyKey,
            OrderStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.createdAt = createdAt;
    }

    /* =========================
       State Transitions
       ========================= */

    public void markPaymentPending() {
        requireState(OrderStatus.CREATED);
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void markPaid() {
        requireState(OrderStatus.PAYMENT_PENDING);
        this.status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        requireState(OrderStatus.PAYMENT_PENDING);
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void markPaymentTimeout() {
        requireState(OrderStatus.PAYMENT_PENDING);
        this.status = OrderStatus.PAYMENT_TIMEOUT;
    }

    public void cancel() {
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Paid order cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /* =========================
       Guards
       ========================= */
    private void requireState(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Invalid transition from " + status + " (expected " + expected + ")"
            );
        }
    }

    /* =========================
       Getters (no setters)
       ========================= */
    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /* =========================
       Equality (identity-based)
       ========================= */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
