package com.aiblog.dto;

import com.aiblog.model.AestheticsArticle;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AestheticsArticleResponse {

    private Long id;
    private String title;
    private String author;
    private String content;
    private String contentHtml;
    private String summary;
    private List<String> tags;
    private String coverImageUrl;
    private Boolean published;
    private AestheticsDimensionResponse sensoryDimension;
    private AestheticsDimensionResponse domainDimension;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AestheticsArticleResponse fromEntity(AestheticsArticle article, String contentHtml) {
        AestheticsArticleResponse response = new AestheticsArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setAuthor(article.getAuthor());
        response.setContent(article.getContent());
        response.setContentHtml(contentHtml);
        response.setSummary(article.getSummary());
        response.setTags(article.getTags());
        response.setCoverImageUrl(article.getCoverImageUrl());
        response.setPublished(article.getPublished());
        response.setSensoryDimension(AestheticsDimensionResponse.fromEntity(article.getSensoryDimension()));
        response.setDomainDimension(AestheticsDimensionResponse.fromEntity(article.getDomainDimension()));
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        return response;
    }
}
