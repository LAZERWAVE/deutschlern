
package com.deutschlern.service;

import com.deutschlern.document.VocabularyDocument;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.repository.VocabularyRepository;
import com.deutschlern.repository.VocabularySearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyIndexService {

    private final VocabularyRepository vocabularyRepository;
    private final VocabularySearchRepository searchRepository;

    // Copy one vocabulary record into Elasticsearch
    public void index(Vocabulary vocabulary) {
        VocabularyDocument document =
                VocabularyDocument.from(vocabulary);

        searchRepository.save(document);
    }

    public void indexAll(List<Vocabulary> vocabularies) {
        List<VocabularyDocument> documents = vocabularies.stream()
                .map(VocabularyDocument::from)
                .toList();

        searchRepository.saveAll(documents);
    }

    // Remove a vocabulary document from Elasticsearch
    public void delete(Long vocabularyId) {
        searchRepository.deleteById(vocabularyId);
    }

    // Rebuild the entire Elasticsearch index from PostgreSQL
    public long rebuildIndex() {
        List<Vocabulary> vocabularies =
                vocabularyRepository.findAll();

        List<VocabularyDocument> documents = vocabularies.stream()
                .map(VocabularyDocument::from)
                .toList();

        searchRepository.saveAll(documents);

        return documents.size();
    }
}
