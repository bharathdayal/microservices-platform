package com.example.order_service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private UUID aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    protected OutboxEvent(){}

    public OutboxEvent(UUID aggregateId,
                       String eventType,
                       String payload){

        this.id=UUID.randomUUID();
        this.aggregateType="ORDER";
        this.aggregateId=aggregateId;
        this.eventType=eventType;
        this.payload=payload;
        this.status=OutboxStatus.PENDING;
        this.createdAt=Instant.now();

    }

    /* ===============================
       FACTORY METHOD (IMPORTANT)
       =============================== */
    public static OutboxEvent create(
            UUID aggregateId,
            Enum<?> eventType,
            Object payload
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return new OutboxEvent(
                    aggregateId,
                    eventType.name(),
                    mapper.writeValueAsString(payload)
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox payload", e
            );
        }
    }


}
