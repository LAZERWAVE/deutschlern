package com.deutschlern.service;

import com.deutschlern.dto.VocabularyRequest;
import com.deutschlern.dto.VocabularyResponse;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.exception.DuplicateResourceException;
import com.deutschlern.exception.ResourceNotFoundException;
import com.deutschlern.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyRepository repository;

    public VocabularyService(VocabularyRepository repository) {
        this.repository = repository;
    }

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

        return VocabularyResponse.from(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Vocabulary vocabulary = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Vocabulary not found: " + id)
        );

        repository.delete(vocabulary);
    }
}
