package com.deutschlern.service;

import com.deutschlern.document.VocabularyDocument;
import com.deutschlern.repository.VocabularySearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularySearchService {

    private final VocabularySearchRepository repository;

    public Page<VocabularyDocument> search(
            String keyword,
            String level,
            String article,
            Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword must not be empty");
        }

        boolean hasLevel = level != null && !level.isBlank();
        boolean hasArticle = article != null && !article.isBlank();

        String normalizedLevel = hasLevel ? level.trim().toUpperCase() : null;
        String normalizedArticle = hasArticle ? article.trim().toUpperCase() : null;

        if (hasLevel && hasArticle) {
            return repository.searchWithFilters(
                    keyword.trim(), normalizedLevel, normalizedArticle, pageable);
        }

        if (hasLevel) {
            return repository.searchByKeywordAndLevel(
                    keyword.trim(), normalizedLevel, pageable);
        }

        if (hasArticle) {
            return repository.searchByKeywordAndArticle(
                    keyword.trim(), normalizedArticle, pageable);
        }

        return repository.searchByKeyword(keyword.trim(), pageable);
    }
}
