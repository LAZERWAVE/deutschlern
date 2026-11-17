package com.deutschlern.messaging.consumer;

import com.deutschlern.entity.LearningActivity;
import com.deutschlern.event.WordLearnedEvent;
import com.deutschlern.repository.LearningActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WordLearnedConsumer {

    private final LearningActivityRepository repository;

    @KafkaListener(
            topics = "word-learned",
            groupId = "deutschlern-progress"
    )
    @Transactional
    public void consume(WordLearnedEvent event) {

        LearningActivity activity = new LearningActivity();

        activity.setStudentId(event.studentId());
        activity.setVocabularyId(event.vocabularyId());
        activity.setGermanWord(event.germanWord());
        activity.setLevel(event.level());
        activity.setLearnedAt(event.occurredAt());

        repository.save(activity);
    }
}
