package com.portfolio.showcase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new Project.
 * Used for POST requests to create projects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project creation request data")
public class ProjectCreateDto {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Project title", example = "AI Chat Interface", required = true)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 2000, message = "Description must be between 1 and 2000 characters")
    @Schema(description = "Project description", example = "A modern conversational AI interface with real-time streaming responses.", required = true)
    private String description;

    @NotBlank(message = "Category is required")
    @Schema(description = "Project category", example = "ai", allowableValues = {"web", "mobile", "ai", "data"}, required = true)
    private String category;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Date must be in YYYY-MM format")
    @Schema(description = "Project date in YYYY-MM format", example = "2024-01", required = true)
    private String date;

    @NotNull(message = "Tools array is required")
    @Schema(description = "List of technologies/tools used", example = "[\"React\", \"OpenAI\", \"Framer Motion\"]", required = true)
    private List<String> tools;

    @NotBlank(message = "Color is required")
    @Size(max = 10, message = "Color must be at most 10 characters")
    @Schema(description = "Color index for UI display", example = "0", required = true)
    private String color;

    @Size(max = 500, message = "Link must be at most 500 characters")
    @Schema(description = "Optional link to project demo", example = "https://demo.example.com")
    private String link;

    @Size(max = 500, message = "GitHub URL must be at most 500 characters")
    @Schema(description = "Optional link to GitHub repository", example = "https://github.com/user/project")
    private String github;

    @Size(max = 500, message = "Image URL must be at most 500 characters")
    @Schema(description = "Image filename for the project", example = "project-ai-chat.jpg")
    private String imageUrl;
}
