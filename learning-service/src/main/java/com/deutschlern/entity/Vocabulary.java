
package com.deutschlern.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vocabulary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vocabulary_german_word",
                        columnNames = "german_word"
                )
        },
        indexes = {
                @Index(name = "idx_vocabulary_level", columnList = "level"),
                @Index(name = "idx_vocabulary_article", columnList = "article")
        }
)
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "german_word", nullable = false, length = 100)
    private String germanWord;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Article article;

    @Column(name = "plural_form", length = 100)
    private String pluralForm;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String englishTranslation;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String indonesianTranslation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Level level;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Article {
        DER, DIE, DAS
    }

    public enum Level {
        A1, A2, B1, B2, C1, C2
    }
}
