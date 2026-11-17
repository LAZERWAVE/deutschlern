
package com.deutschlern.dto;

import com.deutschlern.entity.Vocabulary;
import java.time.LocalDateTime;

public record VocabularyResponse(
        Long id,
        String germanWord,
        Vocabulary.Article article,
        String pluralForm,
        String englishTranslation,
        String indonesianTranslation,
        Vocabulary.Level level,
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
