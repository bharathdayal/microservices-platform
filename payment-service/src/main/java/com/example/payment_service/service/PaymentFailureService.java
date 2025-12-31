package com.example.payment_service.service;

import com.example.payment_service.domain.PaymentLedger;
import com.example.payment_service.domain.PaymentStatus;
import com.example.payment_service.repo.PaymentLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentFailureService {

    private final PaymentLedgerRepository repo;
    private final PaymentEventPublisher publisher;

    public PaymentFailureService(PaymentLedgerRepository repo, PaymentEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID orderId) {

        // Idempotency guard
        if (repo.existsByOrderId(orderId)) {
            return;
        }

        repo.save(new PaymentLedger(orderId, PaymentStatus.FAILED));
        publisher.publishFailed(orderId);
    }
}
