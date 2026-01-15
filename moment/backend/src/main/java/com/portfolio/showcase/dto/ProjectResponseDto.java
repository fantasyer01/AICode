package com.portfolio.showcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for Project API responses.
 * Matches the frontend TypeScript Project interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Project response data")
public class ProjectResponseDto {

    @Schema(description = "Unique project identifier", example = "1")
    private Long id;

    @Schema(description = "Project title", example = "AI Chat Interface")
    private String title;

    @Schema(description = "Project description", example = "A modern conversational AI interface with real-time streaming responses.")
    private String description;

    @Schema(description = "Project category", example = "ai", allowableValues = {"web", "mobile", "ai", "data"})
    private String category;

    @Schema(description = "Project date in YYYY-MM format", example = "2024-01")
    private String date;

    @Schema(description = "Project year", example = "2024")
    private Integer year;

    @Schema(description = "List of technologies/tools used", example = "[\"React\", \"OpenAI\", \"Framer Motion\"]")
    private List<String> tools;

    @Schema(description = "Color index for UI display", example = "0")
    private String color;

    @Schema(description = "Optional link to project demo", example = "https://demo.example.com")
    private String link;

    @Schema(description = "Optional link to GitHub repository", example = "https://github.com/user/project")
    private String github;
}
