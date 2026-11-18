package com.deutschlern.controller;

import com.deutschlern.dto.WordLearnedRequest;
import com.deutschlern.event.WordLearnedEvent;
import com.deutschlern.messaging.producer.WordLearnedProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/learning-activities")
@RequiredArgsConstructor
public class LearningActivityController {

    private final WordLearnedProducer producer;

    @PostMapping("/learn")
    public ResponseEntity<String> learnWord(
            @Valid @RequestBody WordLearnedRequest request
    ) {
        WordLearnedEvent event = new WordLearnedEvent(
                request.studentId(),
                request.vocabularyId(),
                request.germanWord(),
                request.level(),
                LocalDateTime.now()
        );

        producer.publish(event);

        return ResponseEntity.accepted()
                .body("Learning activity event submitted to Kafka");
    }
}
