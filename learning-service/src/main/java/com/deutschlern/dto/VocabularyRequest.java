package com.deutschlern.dto;

import com.deutschlern.entity.Vocabulary;
import com.deutschlern.entity.Vocabulary.Article;
import com.deutschlern.entity.Vocabulary.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record VocabularyRequest(

        @NotBlank
        @Size(max = 100)
        String germanWord,

        @NotNull
        Article article,

        @Size(max = 100)
        String pluralForm,

        @NotBlank
        @Size(max = 150)
        String englishTranslation,

        @NotBlank
        @Size(max = 150)
        String indonesianTranslation,

        @NotNull
        Level level

) {
    public static record VocabularyResponse(
            Long id,
            String germanWord,
            Article article,
            String pluralForm,
            String englishTranslation,
            String indonesianTranslation,
            Level level,
            LocalDateTime createdAt
    ) {

        public static VocabularyResponse from(Vocabulary vocabulary) {
            return new VocabularyResponse(
                    vocabulary.getId(),
                    vocabulary.getGermanWord(),
                    vocabulary.getArticle(),
                    vocabulary.getPluralForm(),
                    vocabulary.getEnglishTranslation(),
                    vocabulary.getIndonesianTranslation(),
                    vocabulary.getLevel(),
                    vocabulary.getCreatedAt()
            );
        }
    }
}
