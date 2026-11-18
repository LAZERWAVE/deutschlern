package com.deutschlern.document;

import com.deutschlern.entity.Vocabulary;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "vocabulary")
public class VocabularyDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String germanWord;

    @Field(type = FieldType.Keyword)
    private String article;

    @Field(type = FieldType.Text)
    private String englishTranslation;

    @Field(type = FieldType.Text)
    private String indonesianTranslation;

    @Field(type = FieldType.Keyword)
    private String level;

    public static VocabularyDocument from(Vocabulary vocabulary) {
        VocabularyDocument document = new VocabularyDocument();
        document.setId(vocabulary.getId());
        document.setGermanWord(vocabulary.getGermanWord());
        document.setArticle(vocabulary.getArticle().name());
        document.setEnglishTranslation(vocabulary.getEnglishTranslation());
        document.setIndonesianTranslation(vocabulary.getIndonesianTranslation());
        document.setLevel(vocabulary.getLevel().name());
        return document;
    }
}
