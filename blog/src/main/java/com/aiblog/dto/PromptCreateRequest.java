package com.aiblog.dto;

import com.aiblog.model.PromptSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PromptCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private String notes;

    @NotNull(message = "Source is required")
    private PromptSource source;

    @Size(max = 200, message = "Model must be at most 200 characters")
    private String model;

    private List<String> tags;

    private Boolean published;
}
