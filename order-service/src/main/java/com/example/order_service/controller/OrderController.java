package com.example.order_service.controller;

import com.example.order_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service=service;
    }


    @PostMapping
    public ResponseEntity<UUID> creatOrder(
            @RequestHeader("Idempotency-Key")String key,
            @RequestHeader("X-User-Id") String userId
    ) {
        return new ResponseEntity<>(service.createOrder(userId,key),HttpStatus.OK);
    }
}
