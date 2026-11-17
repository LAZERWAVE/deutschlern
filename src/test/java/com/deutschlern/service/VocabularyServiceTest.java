package com.deutschlern.service;

import com.deutschlern.dto.VocabularyRequest;
import com.deutschlern.dto.VocabularyResponse;
import com.deutschlern.entity.Vocabulary;
import com.deutschlern.exception.DuplicateResourceException;
import com.deutschlern.exception.ResourceNotFoundException;
import com.deutschlern.repository.VocabularyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceTest {

    @Mock
    private VocabularyRepository repository;

    @InjectMocks
    private VocabularyService service;

    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        vocabulary = new Vocabulary();
        vocabulary.setId(1L);
        vocabulary.setGermanWord("Haus");
        vocabulary.setArticle(Vocabulary.Article.DAS);
        vocabulary.setPluralForm("Häuser");
        vocabulary.setEnglishTranslation("House");
        vocabulary.setIndonesianTranslation("Rumah");
        vocabulary.setLevel(Vocabulary.Level.A1);
    }

    @Test
    void getAll_shouldReturnVocabularyList() {
        when(repository.findAll()).thenReturn(List.of(vocabulary));

        List<VocabularyResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Haus", result.get(0).germanWord());
        verify(repository).findAll();
    }

    @Test
    void getById_shouldReturnVocabulary_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(vocabulary));

        VocabularyResponse result = service.getById(1L);

        assertEquals("Haus", result.germanWord());
        assertEquals(Vocabulary.Article.DAS, result.article());
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getById(99L)
        );
    }

    @Test
    void create_shouldSaveVocabulary() {
        VocabularyRequest request = new VocabularyRequest(
                "Haus",
                Vocabulary.Article.DAS,
                "Häuser",
                "House",
                "Rumah",
                Vocabulary.Level.A1
        );

        when(repository.existsByGermanWordIgnoreCase("Haus"))
                .thenReturn(false);

        when(repository.save(any(Vocabulary.class)))
                .thenAnswer(invocation -> {
                    Vocabulary saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        VocabularyResponse result = service.create(request);

        assertEquals("Haus", result.germanWord());
        assertEquals(1L, result.id());

        verify(repository).save(any(Vocabulary.class));
    }

    @Test
    void create_shouldThrowException_whenDuplicate() {
        VocabularyRequest request = new VocabularyRequest(
                "Haus",
                Vocabulary.Article.DAS,
                "Häuser",
                "House",
                "Rumah",
                Vocabulary.Level.A1
        );

        when(repository.existsByGermanWordIgnoreCase("Haus"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any(Vocabulary.class));
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never()).delete(any(Vocabulary.class));
    }
}
