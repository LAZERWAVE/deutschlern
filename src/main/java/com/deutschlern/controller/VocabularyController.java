package com.deutschlern.controller;

import com.deutschlern.dto.VocabularyRequest;
import com.deutschlern.dto.VocabularyResponse;
import com.deutschlern.dto.VocabularyStatisticsResponse;
import com.deutschlern.dto.projection.VocabularyLevelAnalytics;
import com.deutschlern.dto.projection.VocabularyLevelComparison;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.service.VocabularyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyController {

    private final VocabularyService service;

    public VocabularyController(VocabularyService service) {
        this.service = service;
    }

    @GetMapping
    public List<VocabularyResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public VocabularyResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/level/{level}")
    public List<VocabularyResponse> getByLevel(
            @PathVariable Vocabulary.Level level) {
        return service.getByLevel(level);
    }

    @GetMapping("/article/{article}")
    public List<VocabularyResponse> getByArticle(
            @PathVariable Vocabulary.Article article) {
        return service.getByArticle(article);
    }

    @GetMapping("/search")
    public Page<VocabularyResponse> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("germanWord").ascending()
        );

        return service.search(keyword.trim(), pageable);
    }

    @GetMapping("/statistics")
    public VocabularyStatisticsResponse getStatistics() {
        return service.getStatistics();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VocabularyResponse create(
            @Valid @RequestBody VocabularyRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public VocabularyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody VocabularyRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VocabularyResponse> createBulk(
            @Valid @RequestBody List<@Valid VocabularyRequest> requests) {

        return service.createBulk(requests);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/analytics/levels")
    public List<VocabularyLevelAnalytics> getLevelAnalytics() {
        return service.getLevelAnalytics();
    }

    @GetMapping("/analytics/levels/comparison")
    public List<VocabularyLevelComparison> getLevelComparison() {
        return service.getLevelComparison();
    }
}
