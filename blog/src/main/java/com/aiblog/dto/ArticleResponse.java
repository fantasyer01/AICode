package com.aiblog.dto;

import com.aiblog.model.Article;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ArticleResponse {

    private Long id;
    private String title;
    private String author;
    private String content;
    private String contentHtml;
    private String summary;
    private List<String> tags;
    private String category;
    private String coverImageUrl;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArticleResponse fromEntity(Article article, String contentHtml) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setAuthor(article.getAuthor());
        response.setContent(article.getContent());
        response.setContentHtml(contentHtml);
        response.setSummary(article.getSummary());
        response.setTags(article.getTags());
        response.setCategory(article.getCategory());
        response.setCoverImageUrl(article.getCoverImageUrl());
        response.setPublished(article.getPublished());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        return response;
    }
}
