package com.deutschlern.controller;

import com.deutschlern.document.VocabularyDocument;
import com.deutschlern.service.VocabularySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vocabulary/search/elastic")
@RequiredArgsConstructor
public class VocabularySearchController {

    private final VocabularySearchService searchService;

    @GetMapping
    public Page<VocabularyDocument> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String article,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return searchService.search(keyword, level, article, pageable);
    }

}
