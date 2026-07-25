package com.aiblog.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ArticleUpdateRequest {

    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    @Size(max = 200, message = "Author must be at most 200 characters")
    private String author;

    private String content;

    @Size(max = 1000, message = "Summary must be at most 1000 characters")
    private String summary;

    private List<String> tags;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    /**
     * Cover image URL. Must start with '/', 'http://', or 'https://'.
     * Upload the image first via POST /api/images/upload to obtain a URL.
     */
    @Pattern(
        regexp = "^(https?://|/).*",
        message = "coverImageUrl must be a URL starting with '/', 'http://', or 'https://'. " +
                  "Upload the image first via POST /api/images/upload"
    )
    private String coverImageUrl;

    private Boolean published;
}
