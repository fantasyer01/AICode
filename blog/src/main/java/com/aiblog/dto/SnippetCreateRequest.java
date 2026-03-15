package com.aiblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnippetCreateRequest {

    @NotBlank(message = "Raw content is required")
    private String rawContent;

    @Size(max = 200, message = "Author must be at most 200 characters")
    private String author;
}
