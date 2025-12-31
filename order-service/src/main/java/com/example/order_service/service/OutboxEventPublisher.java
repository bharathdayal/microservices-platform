package com.example.order_service.service;

import com.example.order_service.domain.OutboxStatus;
import com.example.order_service.repo.OutboxRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String,String> kafka;
    private final String topic;

    public OutboxEventPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafka, @Value("${order.topic.name}") String topic) {
        this.outboxRepository = outboxRepository;
        this.kafka = kafka;
        this.topic = topic;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {

        var events=outboxRepository.findByStatus(OutboxStatus.PENDING);

        for(var e:events) {
            try {
                kafka.send(
                        topic,
                        e.getAggregateId().toString(),
                        e.getPayload()).get();
                        e.setStatus(OutboxStatus.SENT);

            } catch (Exception ex) {
                e.setStatus(OutboxStatus.FAILED);
            }
        }
    }
}
