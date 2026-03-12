package com.aiblog.dto;

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

    private String coverImage;
}
