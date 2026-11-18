
package com.deutschlern.repository;

import com.deutschlern.dto.projection.VocabularyLevelAnalytics;
import com.deutschlern.dto.projection.VocabularyLevelComparison;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.entity.Vocabulary.Article;
import com.deutschlern.entity.Vocabulary.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VocabularyRepository
        extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByLevel(Level level);

    List<Vocabulary> findByArticle(Article article);

    boolean existsByGermanWordIgnoreCase(String germanWord);

    Page<Vocabulary> findByGermanWordContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    @Query(value = """
        WITH levels AS (
            SELECT *
            FROM (VALUES
                ('A1', 1),
                ('A2', 2),
                ('B1', 3),
                ('B2', 4),
                ('C1', 5),
                ('C2', 6)
            ) AS level_data(level, sort_order)
        ),
        level_counts AS (
            SELECT
                l.level,
                l.sort_order,
                COUNT(v.id) AS total_words
            FROM levels l
            LEFT JOIN vocabulary v
                ON v.level = l.level
            GROUP BY l.level, l.sort_order
        )
        SELECT
            level AS "level",
            total_words AS "totalWords",
            ROUND(
                100.0 * total_words /
                NULLIF(SUM(total_words) OVER (), 0),
                2
            ) AS "percentage",
            DENSE_RANK() OVER (
                ORDER BY total_words DESC
            ) AS "ranking"
        FROM level_counts
        ORDER BY sort_order
    """, nativeQuery = true)
    List<VocabularyLevelAnalytics> getLevelAnalytics();

    @Query(value = """
        WITH levels AS (
            SELECT *
            FROM (VALUES
                ('A1', 1),
                ('A2', 2),
                ('B1', 3),
                ('B2', 4),
                ('C1', 5),
                ('C2', 6)
            ) AS level_data(level, sort_order)
        ),
        level_counts AS (
            SELECT
                l.level,
                l.sort_order,
                COUNT(v.id) AS total_words
            FROM levels l
            LEFT JOIN vocabulary v
                ON v.level = l.level
            GROUP BY l.level, l.sort_order
        ),
        comparisons AS (
            SELECT
                level,
                total_words,
                LAG(total_words, 1, 0) OVER (
                    ORDER BY sort_order
                ) AS previous_level_words,
                ROW_NUMBER() OVER (
                    ORDER BY sort_order
                ) AS row_number
            FROM level_counts
        )
        SELECT
            level AS "level",
            total_words AS "totalWords",
            previous_level_words AS "previousLevelWords",
            total_words - previous_level_words AS "difference",
            row_number AS "rowNumber"
        FROM comparisons
        ORDER BY row_number
    """, nativeQuery = true)
    List<VocabularyLevelComparison> getLevelComparison();
}
