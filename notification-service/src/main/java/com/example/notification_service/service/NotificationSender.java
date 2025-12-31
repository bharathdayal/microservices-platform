package com.example.notification_service.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationSender {

    public void sendSuccess(UUID orderId) {
        System.out.println("Payment SUCCESS notification sent for order " + orderId);
    }

    public void sendFailure(UUID orderId) {
        System.out.println("Payment FAILED notification sent for order " + orderId);
    }

    public void sendPaymentTimeout(UUID orderId) {
        System.out.println(
                "Payment TIMEOUT notification sent for order " + orderId
        );
    }
}
