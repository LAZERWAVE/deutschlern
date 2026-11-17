package com.deutschlern.service;

import com.deutschlern.dto.VocabularyRequest;
import com.deutschlern.dto.VocabularyResponse;
import com.deutschlern.dto.VocabularyStatisticsResponse;
import com.deutschlern.dto.projection.VocabularyLevelAnalytics;
import com.deutschlern.dto.projection.VocabularyLevelComparison;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.exception.DuplicateResourceException;
import com.deutschlern.exception.ResourceNotFoundException;
import com.deutschlern.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyRepository repository;
    private final VocabularyIndexService vocabularyIndexService;

    public List<VocabularyResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(VocabularyResponse::from)
                .toList();
    }

    public VocabularyResponse getById(Long id) {
        return VocabularyResponse.from(
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Vocabulary not found: " + id
                                ))
        );
    }

    public List<VocabularyResponse> getByLevel(Vocabulary.Level level) {
        return repository.findByLevel(level)
                .stream()
                .map(VocabularyResponse::from)
                .toList();
    }

    public List<VocabularyResponse> getByArticle(Vocabulary.Article article) {
        return repository.findByArticle(article)
                .stream()
                .map(VocabularyResponse::from)
                .toList();
    }

    public Page<VocabularyResponse> search(String keyword, Pageable pageable) {
        return repository
                .findByGermanWordContainingIgnoreCase(keyword, pageable)
                .map(VocabularyResponse::from);
    }

    @Transactional
    public VocabularyResponse create(VocabularyRequest request) {
        String word = request.germanWord().trim();

        if (repository.existsByGermanWordIgnoreCase(word)) {
            throw new DuplicateResourceException(
                    "Vocabulary already exists: " + word
            );
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setGermanWord(word);
        vocabulary.setArticle(request.article());
        vocabulary.setPluralForm(request.pluralForm());
        vocabulary.setEnglishTranslation(request.englishTranslation());
        vocabulary.setIndonesianTranslation(request.indonesianTranslation());
        vocabulary.setLevel(request.level());
        vocabularyIndexService.index(vocabulary);
        return VocabularyResponse.from(repository.save(vocabulary));
    }

    @Transactional
    public VocabularyResponse update(Long id, VocabularyRequest request) {
        Vocabulary existing = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vocabulary not found: " + id
                        ));

        existing.setGermanWord(request.germanWord().trim());
        existing.setArticle(request.article());
        existing.setPluralForm(request.pluralForm());
        existing.setEnglishTranslation(request.englishTranslation());
        existing.setIndonesianTranslation(request.indonesianTranslation());
        existing.setLevel(request.level());

        vocabularyIndexService.index(existing);
        return VocabularyResponse.from(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Vocabulary vocabulary = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Vocabulary not found: " + id)
        );

        vocabularyIndexService.delete(id);
        repository.delete(vocabulary);
    }

    public VocabularyStatisticsResponse getStatistics() {

        List<Vocabulary> vocabularyList = repository.findAll();

        Map<Vocabulary.Level, Long> wordsByLevel =
                vocabularyList.stream()
                        .collect(Collectors.groupingBy(
                                Vocabulary::getLevel,
                                Collectors.counting()
                        ));

        Map<Vocabulary.Article, Long> wordsByArticle =
                vocabularyList.stream()
                        .collect(Collectors.groupingBy(
                                Vocabulary::getArticle,
                                Collectors.counting()
                        ));

        return new VocabularyStatisticsResponse(
                vocabularyList.size(),
                wordsByLevel,
                wordsByArticle
        );
    }

    @Transactional
    public List<VocabularyResponse> createBulk(
            List<VocabularyRequest> requests) {

        Set<String> words = new HashSet<>();
        List<Vocabulary> vocabularyList = new ArrayList<>();

        for (VocabularyRequest request : requests) {

            String word = request.germanWord().trim();

            // Check for duplicates within this batch
            if (!words.add(word.toLowerCase())) {
                throw new DuplicateResourceException(
                        "Duplicate word in request: " + word
                );
            }

            // Check whether the word already exists in the database
            if (repository.existsByGermanWordIgnoreCase(word)) {
                throw new DuplicateResourceException(
                        "Vocabulary already exists: " + word
                );
            }

            Vocabulary vocabulary = new Vocabulary();
            vocabulary.setGermanWord(word);
            vocabulary.setArticle(request.article());
            vocabulary.setPluralForm(request.pluralForm());
            vocabulary.setEnglishTranslation(request.englishTranslation());
            vocabulary.setIndonesianTranslation(request.indonesianTranslation());
            vocabulary.setLevel(request.level());

            vocabularyList.add(vocabulary);
        }

        // Save everything to PostgreSQL
        List<Vocabulary> savedVocabularies =
                repository.saveAll(vocabularyList);

        // Synchronize all saved records with Elasticsearch
        vocabularyIndexService.indexAll(savedVocabularies);

        // Return API responses
        return savedVocabularies.stream()
                .map(VocabularyResponse::from)
                .toList();
    }


    public List<VocabularyLevelAnalytics> getLevelAnalytics() {
        return repository.getLevelAnalytics();
    }

    public List<VocabularyLevelComparison> getLevelComparison() {
        return repository.getLevelComparison();
    }
}
