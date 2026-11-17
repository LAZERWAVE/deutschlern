
package com.deutschlern.repository;

import com.deutschlern.entity.Vocabulary;
import com.deutschlern.entity.Vocabulary.Article;
import com.deutschlern.entity.Vocabulary.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
