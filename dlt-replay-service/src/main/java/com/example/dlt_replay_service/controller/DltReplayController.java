package com.example.dlt_replay_service.controller;

import com.example.dlt_replay_service.service.DltReplayService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/admin/dlt")
public class DltReplayController {

    private final KafkaConsumer<String, String> consumer;
    private final DltReplayService replayService;

    public DltReplayController(
            KafkaConsumer<String, String> consumer,
            DltReplayService replayService
    ) {
        this.consumer = consumer;
        this.replayService = replayService;
    }

    @PostMapping("/replay")
    public ResponseEntity<String> replayOne(
            @RequestParam String topic,
            @RequestParam int partition,
            @RequestParam long offset
    ) {

        TopicPartition tp = new TopicPartition(topic, partition);
        consumer.assign(List.of(tp));
        consumer.seek(tp, offset);

        ConsumerRecords<String, String> records =
                consumer.poll(Duration.ofSeconds(3));

        if (records.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("No record found at offset");
        }

        ConsumerRecord<String, String> record =
                records.iterator().next();

        replayService.replay(record);

        return ResponseEntity.ok(
                "Replayed message to original topic"
        );
    }
}
