package com.example.dlt_replay_service.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DltReplayService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DltReplayService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Replay a single DLT payload to original topic
     */
    public void replay(
            ConsumerRecord<String, String> record
    ) {

        Header originalTopicHeader =
                record.headers().lastHeader("x-original-topic");

        if (originalTopicHeader == null) {
            throw new IllegalStateException(
                    "Missing x-original-topic header"
            );
        }

        String originalTopic =
                new String(originalTopicHeader.value(), StandardCharsets.UTF_8);

        kafkaTemplate.send(
                originalTopic,
                record.key(),
                record.value()
        );
    }
}
