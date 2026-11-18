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
@Table(name = "learning_activity")
public class LearningActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long vocabularyId;

    @Column(nullable = false)
    private String germanWord;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private LocalDateTime learnedAt;
}
