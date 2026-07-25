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
import org.springframework.mock.web.MockMultipartFile;
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

    // ---------- cover image upload ----------

    /** A 1x1 PNG (89 bytes) - valid PNG header, used to exercise the upload pipeline. */
    private static final byte[] PNG_1x1 = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82
    };

    @Test
    void updateCoverImage_setsCoverWhenAbsent_andReturnsUrl() throws Exception {
        Article article = new Article();
        article.setTitle("No Cover Yet");
        article.setContent("body");
        Article saved = articleRepository.save(article);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", MediaType.IMAGE_PNG_VALUE, PNG_1x1);

        mockMvc.perform(multipart("/api/articles/" + saved.getId() + "/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.coverImageUrl",
                        matchesPattern("/images/[a-f0-9-]+\\.png")));
    }

    @Test
    void updateCoverImage_replacesExistingCover() throws Exception {
        Article article = new Article();
        article.setTitle("Has Old Cover");
        article.setContent("body");
        article.setCoverImageUrl("/images/old.png");
        Article saved = articleRepository.save(article);

        MockMultipartFile file = new MockMultipartFile(
                "file", "new-cover.png", MediaType.IMAGE_PNG_VALUE, PNG_1x1);

        mockMvc.perform(multipart("/api/articles/" + saved.getId() + "/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl", not("/images/old.png")))
                .andExpect(jsonPath("$.coverImageUrl",
                        matchesPattern("/images/[a-f0-9-]+\\.png")));
    }

    @Test
    void updateCoverImage_withoutApiKey_shouldReturn401() throws Exception {
        Article article = new Article();
        article.setTitle("Auth Required");
        article.setContent("body");
        Article saved = articleRepository.save(article);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", MediaType.IMAGE_PNG_VALUE, PNG_1x1);

        mockMvc.perform(multipart("/api/articles/" + saved.getId() + "/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCoverImage_articleNotFound_shouldReturn404() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", MediaType.IMAGE_PNG_VALUE, PNG_1x1);

        mockMvc.perform(multipart("/api/articles/999999/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCoverImage_nonImageContentType_shouldReturn400() throws Exception {
        Article article = new Article();
        article.setTitle("Wrong Type");
        article.setContent("body");
        Article saved = articleRepository.save(article);

        MockMultipartFile file = new MockMultipartFile(
                "file", "evil.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/api/articles/" + saved.getId() + "/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCoverImage_emptyFile_shouldReturn400() throws Exception {
        Article article = new Article();
        article.setTitle("Empty File");
        article.setContent("body");
        Article saved = articleRepository.save(article);

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/articles/" + saved.getId() + "/cover")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isBadRequest());
    }
}
