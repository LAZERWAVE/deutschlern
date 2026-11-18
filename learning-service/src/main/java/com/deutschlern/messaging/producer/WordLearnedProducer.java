package com.deutschlern.messaging.producer;

import com.deutschlern.event.WordLearnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WordLearnedProducer {

    private static final String TOPIC = "word-learned";

    private final KafkaTemplate<String, WordLearnedEvent> kafkaTemplate;

    public void publish(WordLearnedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.studentId().toString(),
                event
        );
    }
}
