package com.example.order_service.service;

import com.example.order_service.domain.OutboxStatus;
import com.example.order_service.repo.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String,Object> kafka;
    private final String topic;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, Object> kafka, @Value("${order.topic.name}") String topic,ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafka = kafka;
        this.topic = topic;
        this.objectMapper=objectMapper;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {

        var events = outboxRepository.findByStatus(OutboxStatus.PENDING);

        for (var e : events) {
            try {
                Object event =
                        objectMapper.readValue(e.getPayload(), Object.class);

                kafka.send(
                        topic,
                        e.getAggregateId().toString(),
                        event
                ).get();

                e.setStatus(OutboxStatus.SENT);

            } catch (Exception ex) {
                e.setStatus(OutboxStatus.FAILED);
            }
        }
    }
}
