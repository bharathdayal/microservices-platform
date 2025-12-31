package com.example.payment_service.service;

import com.example.payment_service.domain.PaymentLedger;
import com.example.payment_service.domain.PaymentStatus;
import com.example.payment_service.repo.PaymentLedgerRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class PaymentProcessor {

    private final PaymentLedgerRepository repo;
    private final StringRedisTemplate redis;
    private final PaymentEventPublisher publisher;
    private final PaymentFailureService failureService;



    public PaymentProcessor(PaymentLedgerRepository repo, StringRedisTemplate redis, PaymentEventPublisher publisher,PaymentFailureService failureService) {
        this.repo = repo;
        this.redis = redis;
        this.publisher = publisher;
        this.failureService=failureService;
    }

    @Transactional
    public void processPayment(UUID orderId) {

        String redisKey = "payment:" + orderId;

        // ---- Redis idempotency (fast path)
        Boolean first =
                redis.opsForValue()
                        .setIfAbsent(redisKey, "1", Duration.ofMinutes(10));

        if (Boolean.FALSE.equals(first)) {
            return;
        }

        // ---- DB idempotency (hard guarantee)
        if (repo.findByOrderId(orderId).isPresent()) {
            return;
        }

        try {
            // Simulate external payment
            if (Math.random() < 0.7) {
                repo.save(new PaymentLedger(orderId, PaymentStatus.SUCCESS));
                publisher.publishCompleted(orderId);
            } else {
                throw new RuntimeException("Payment gateway failure");
            }

        } catch (Exception ex) {
            failureService.recordFailure(orderId);
        }
    }


}
