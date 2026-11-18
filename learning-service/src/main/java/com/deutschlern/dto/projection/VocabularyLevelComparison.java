package com.deutschlern.dto.projection;

public interface VocabularyLevelComparison {

    String getLevel();

    Long getTotalWords();

    Long getPreviousLevelWords();

    Long getDifference();

    Long getRowNumber();
}