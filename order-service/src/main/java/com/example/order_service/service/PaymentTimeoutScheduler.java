package com.example.order_service.service;

import com.example.order_service.domain.Order;
import com.example.order_service.domain.OrderEventType;
import com.example.order_service.domain.OrderStatus;
import com.example.order_service.domain.OutboxEvent;
import com.example.order_service.repo.OrderRepository;
import com.example.order_service.repo.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class PaymentTimeoutScheduler {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(5);

    private final OrderRepository orderRepo;
    private final OutboxRepository outboxRepo;

    public PaymentTimeoutScheduler(
            OrderRepository orderRepo,
            OutboxRepository outboxRepo
    ) {
        this.orderRepo = orderRepo;
        this.outboxRepo = outboxRepo;
    }

    @Scheduled(fixedDelay = 60000) // every minute
    @Transactional
    public void detectTimedOutPayments() {

        Instant cutoff = Instant.now().minus(PAYMENT_TIMEOUT);

        List<Order> timedOutOrders =
                orderRepo.findByStatusAndCreatedAtBefore(
                        OrderStatus.PAYMENT_PENDING,
                        cutoff
                );

        for (Order order : timedOutOrders) {

            order.markPaymentTimeout();

            outboxRepo.save(
                    OutboxEvent.create(
                            order.getId(),
                            OrderEventType.ORDER_PAYMENT_TIMEOUT,
                            Map.of(
                                    "orderId", order.getId().toString(),
                                    "reason", "PAYMENT_TIMEOUT"
                            )
                    )
            );
        }
    }
}
