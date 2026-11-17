
package com.deutschlern.repository;
import com.deutschlern.document.VocabularyDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VocabularySearchRepository
        extends ElasticsearchRepository<VocabularyDocument, Long> {

    // Keyword only
    @Query("""
        {
          "multi_match": {
            "query": "?0",
            "fields": [
              "germanWord^3",
              "englishTranslation^2",
              "indonesianTranslation"
            ],
            "type": "best_fields",
            "fuzziness": "AUTO"
          }
        }
        """)
    Page<VocabularyDocument> searchByKeyword(
            String keyword,
            Pageable pageable
    );

    // Keyword + CEFR level
    @Query("""
        {
          "bool": {
            "must": [{
              "multi_match": {
                "query": "?0",
                "fields": ["germanWord^3", "englishTranslation^2", "indonesianTranslation"],
                "fuzziness": "AUTO"
              }
            }],
            "filter": [{
              "term": { "level": "?1" }
            }]
          }
        }
        """)
    Page<VocabularyDocument> searchByKeywordAndLevel(
            String keyword, String level, Pageable pageable
    );

    // Keyword + article
    @Query("""
        {
          "bool": {
            "must": [{
              "multi_match": {
                "query": "?0",
                "fields": ["germanWord^3", "englishTranslation^2", "indonesianTranslation"],
                "fuzziness": "AUTO"
              }
            }],
            "filter": [{
              "term": { "article": "?1" }
            }]
          }
        }
        """)
    Page<VocabularyDocument> searchByKeywordAndArticle(
            String keyword, String article, Pageable pageable
    );

    // Keyword + CEFR level + article
    @Query("""
        {
          "bool": {
            "must": [{
              "multi_match": {
                "query": "?0",
                "fields": ["germanWord^3", "englishTranslation^2", "indonesianTranslation"],
                "fuzziness": "AUTO"
              }
            }],
            "filter": [
              { "term": { "level": "?1" } },
              { "term": { "article": "?2" } }
            ]
          }
        }
        """)
    Page<VocabularyDocument> searchWithFilters(
            String keyword, String level, String article, Pageable pageable
    );
}
