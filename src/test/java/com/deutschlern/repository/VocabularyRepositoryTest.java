package com.deutschlern.repository;

import com.deutschlern.entity.Vocabulary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VocabularyRepositoryTest {

    @Autowired
    private VocabularyRepository repository;

    @Test
    void getLevelComparison_shouldCompareVocabularyCounts() {
        // Arrange: create test vocabulary
        Vocabulary a1 = createVocabulary(
                "TestHaus", Vocabulary.Level.A1
        );

        Vocabulary a2 = createVocabulary(
                "TestAuto", Vocabulary.Level.A2
        );

        repository.saveAll(List.of(a1, a2));

        // Act: execute the actual PostgreSQL query
        var result = repository.getLevelComparison();

        // Assert: verify the results
        assertThat(result).hasSize(6);

        var a1Result = result.get(0);
        assertThat(a1Result.getLevel()).isEqualTo("A1");
        assertThat(a1Result.getTotalWords()).isGreaterThanOrEqualTo(1);

        var a2Result = result.get(1);
        assertThat(a2Result.getLevel()).isEqualTo("A2");
        assertThat(a2Result.getTotalWords()).isGreaterThanOrEqualTo(1);
    }

    private Vocabulary createVocabulary(
            String word,
            Vocabulary.Level level
    ) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setGermanWord(word);
        vocabulary.setArticle(Vocabulary.Article.DAS);
        vocabulary.setEnglishTranslation("Test");
        vocabulary.setIndonesianTranslation("Tes");
        vocabulary.setLevel(level);

        return vocabulary;
    }


    @Test
    void getLevelAnalytics_shouldCalculatePercentagesAndRankings() {

        // Act: execute the actual PostgreSQL query
        var result = repository.getLevelAnalytics();

        // Assert: all six CEFR levels should be returned
        assertThat(result).hasSize(6);

        // Verify the levels are in the correct order
        assertThat(result)
                .extracting("level")
                .containsExactly("A1", "A2", "B1", "B2", "C1", "C2");

        // Calculate the sum of all percentages
        double percentageTotal = result.stream()
                .mapToDouble(item -> item.getPercentage())
                .sum();

        // Percentages should add up to approximately 100%
        assertThat(percentageTotal)
                .isBetween(99.9, 100.1);

        // Every level should have a ranking
        assertThat(result)
                .allSatisfy(item -> {
                    assertThat(item.getRanking()).isNotNull();
                    assertThat(item.getTotalWords()).isNotNull();
                });
    }

}
