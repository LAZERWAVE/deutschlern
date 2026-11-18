package com.deutschlern.dto;

import com.deutschlern.entity.Vocabulary.Article;
import com.deutschlern.entity.Vocabulary.Level;

import java.util.Map;

public record VocabularyStatisticsResponse(
        long totalWords,
        Map<Level, Long> wordsByLevel,
        Map<Article, Long> wordsByArticle
) {}

