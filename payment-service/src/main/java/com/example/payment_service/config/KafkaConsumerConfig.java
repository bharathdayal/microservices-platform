package com.example.payment_service.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object>
    kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // Manual acknowledgment
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        //Dead Letter Publishing with ORIGINAL TOPIC HEADER
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            // Preserve original topic for replay tooling
                            record.headers().add(
                                    "x-original-topic",
                                    record.topic().getBytes(StandardCharsets.UTF_8)
                            );

                            // Optional but very useful for ops/debugging
                            record.headers().add(
                                    "x-exception-class",
                                    exception.getClass()
                                            .getName()
                                            .getBytes(StandardCharsets.UTF_8)
                            );

                            record.headers().add(
                                    "x-exception-message",
                                    Optional.ofNullable(exception.getMessage())
                                            .orElse("N/A")
                                            .getBytes(StandardCharsets.UTF_8)
                            );

                            // Route to <original-topic>.dlt
                            return new TopicPartition(
                                    record.topic() + ".dlt",
                                    record.partition()
                            );
                        }
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        new FixedBackOff(3000L, 3) // retry 3 times
                );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
