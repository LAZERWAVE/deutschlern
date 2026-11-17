package com.deutschlern.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "learning_activity",
        indexes = {
                @Index(name = "idx_activity_student", columnList = "student_id"),
                @Index(name = "idx_activity_learned_at", columnList = "learned_at")
        }
)
public class LearningActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Column(name = "german_word", nullable = false, length = 100)
    private String germanWord;

    @Column(nullable = false, length = 2)
    private String level;

    @Column(name = "learned_at", nullable = false)
    private LocalDateTime learnedAt;
}
