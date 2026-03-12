package com.aiblog.service;

import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.Article;
import com.aiblog.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MarkdownService markdownService;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article sampleArticle;

    @BeforeEach
    void setUp() {
        sampleArticle = new Article();
        sampleArticle.setId(1L);
        sampleArticle.setTitle("Test Article");
        sampleArticle.setAuthor("GPT-4");
        sampleArticle.setContent("# Hello");
        sampleArticle.setSummary("A test");
        sampleArticle.setTags(List.of("ai", "test"));
        sampleArticle.setCreatedAt(LocalDateTime.now());
        sampleArticle.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("Test Article");
        request.setAuthor("GPT-4");
        request.setContent("# Hello");
        request.setSummary("A test");
        request.setTags(List.of("ai", "test"));

        when(articleRepository.save(any(Article.class))).thenReturn(sampleArticle);
        when(markdownService.renderToHtml("# Hello")).thenReturn("<h1>Hello</h1>");

        ArticleResponse response = articleService.create(request);

        assertThat(response.getTitle()).isEqualTo("Test Article");
        assertThat(response.getAuthor()).isEqualTo("GPT-4");
        assertThat(response.getContentHtml()).isEqualTo("<h1>Hello</h1>");
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void getById_shouldReturnArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));
        when(markdownService.renderToHtml("# Hello")).thenReturn("<h1>Hello</h1>");

        ArticleResponse response = articleService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Article");
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_shouldOnlyUpdateNonNullFields() {
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setTitle("Updated Title");
        // content, author, etc. left null - should not be overwritten

        when(articleRepository.findById(1L)).thenReturn(Optional.of(sampleArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(sampleArticle);
        when(markdownService.renderToHtml(any())).thenReturn("<h1>Hello</h1>");

        articleService.update(1L, request);

        assertThat(sampleArticle.getTitle()).isEqualTo("Updated Title");
        assertThat(sampleArticle.getContent()).isEqualTo("# Hello"); // unchanged
        assertThat(sampleArticle.getAuthor()).isEqualTo("GPT-4"); // unchanged
    }
}
