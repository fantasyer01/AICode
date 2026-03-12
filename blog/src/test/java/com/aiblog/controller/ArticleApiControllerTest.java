package com.aiblog.controller;

import com.aiblog.model.Article;
import com.aiblog.repository.ArticleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    private static final String API_KEY = "your-api-key-here";

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
    }

    @Test
    void createArticle_shouldReturn201() throws Exception {
        String json = """
                {
                    "title": "Test Article",
                    "author": "GPT-4",
                    "content": "# Hello\\nThis is a test.",
                    "summary": "A test article",
                    "tags": ["test", "ai"]
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", API_KEY)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.author").value("GPT-4"))
                .andExpect(jsonPath("$.contentHtml").value(containsString("<h1>Hello</h1>")))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createArticle_withoutApiKey_shouldReturn401() throws Exception {
        String json = """
                {
                    "title": "Test Article",
                    "content": "Some content"
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createArticle_withMissingTitle_shouldReturn400() throws Exception {
        String json = """
                {
                    "content": "Some content"
                }
                """;

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", API_KEY)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void getById_shouldReturnArticle() throws Exception {
        Article article = new Article();
        article.setTitle("Existing Article");
        article.setAuthor("Claude");
        article.setContent("Some markdown content");
        article.setTags(List.of("ai"));
        Article saved = articleRepository.save(article);

        mockMvc.perform(get("/api/articles/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing Article"))
                .andExpect(jsonPath("$.author").value("Claude"));
    }

    @Test
    void getById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/articles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void updateArticle_shouldReturnUpdated() throws Exception {
        Article article = new Article();
        article.setTitle("Original Title");
        article.setContent("Original content");
        article.setTags(List.of("old"));
        Article saved = articleRepository.save(article);

        String json = """
                {
                    "title": "Updated Title",
                    "author": "GPT-4",
                    "tags": ["new", "updated"]
                }
                """;

        mockMvc.perform(put("/api/articles/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", API_KEY)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.author").value("GPT-4"))
                .andExpect(jsonPath("$.content").value("Original content"))
                .andExpect(jsonPath("$.tags", hasSize(2)));
    }

    @Test
    void listArticles_shouldReturnPaginatedResults() throws Exception {
        for (int i = 0; i < 15; i++) {
            Article article = new Article();
            article.setTitle("Article " + i);
            article.setContent("Content " + i);
            article.setTags(List.of("batch"));
            articleRepository.save(article);
        }

        mockMvc.perform(get("/api/articles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listArticles_filterByTag_shouldReturnFiltered() throws Exception {
        Article a1 = new Article();
        a1.setTitle("AI Article");
        a1.setContent("content");
        a1.setTags(List.of("ai", "tech"));
        articleRepository.save(a1);

        Article a2 = new Article();
        a2.setTitle("Food Article");
        a2.setContent("content");
        a2.setTags(List.of("food"));
        articleRepository.save(a2);

        mockMvc.perform(get("/api/articles?tag=ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("AI Article"));
    }
}
