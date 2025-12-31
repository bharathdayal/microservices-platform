package com.example.notification_service.consumer;

import com.example.notification_service.service.NotificationSender;
import com.example.notification_service.domain.NotificationLog;
import com.example.notification_service.domain.NotificationType;
import com.example.notification_service.repo.NotificationLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final NotificationLogRepository repo;
    private final NotificationSender sender;

    public PaymentEventConsumer(
            NotificationLogRepository repo,
            NotificationSender sender
    ) {
        this.repo = repo;
        this.sender = sender;
    }



    @KafkaListener(
            topics = {"payment.completed", "payment.failed","order.payment.timeout"},
            groupId = "notification-microservice"
    )
    @Transactional
    public void consume(Map<String, Object> event) {
        System.out.println("Notification for order " + event);

        UUID orderId =
                UUID.fromString(event.get("orderId").toString());

        String status =
                event.getOrDefault("status", "TIMEOUT").toString();

        NotificationType type;

        switch (status) {
            case "SUCCESS" -> {
                type = NotificationType.PAYMENT_SUCCESS;
                sender.sendSuccess(orderId);
            }
            case "FAILED" -> {
                type = NotificationType.PAYMENT_FAILED;
                sender.sendFailure(orderId);
            }
            default -> {
                type = NotificationType.PAYMENT_TIMEOUT;
                sender.sendPaymentTimeout(orderId);
            }
        }

        if (repo.existsByOrderIdAndType(orderId, type)) {
            return;
        }

        repo.save(new NotificationLog(orderId, type));
    }



}
