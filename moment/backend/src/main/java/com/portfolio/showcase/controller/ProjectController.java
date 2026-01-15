package com.portfolio.showcase.controller;

import com.portfolio.showcase.dto.ErrorResponseDto;
import com.portfolio.showcase.dto.ProjectResponseDto;
import com.portfolio.showcase.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * REST Controller for project-related API endpoints.
 * Provides read-only access to project showcase data.
 */
@RestController
@RequestMapping("/api/projects")
@Validated
@Tag(name = "Projects", description = "Project showcase API endpoints")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private static final int CACHE_MAX_AGE_SECONDS = 300; // 5 minutes

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Get all projects, optionally filtered by category.
     *
     * @param category optional category filter (all, web, mobile, ai, data)
     * @return list of projects
     */
    @GetMapping
    @Operation(
        summary = "Get all projects",
        description = "Retrieves all projects, optionally filtered by category. Results are sorted by date (newest first)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved projects",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category parameter",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects(
            @Parameter(
                description = "Filter by category. Use 'all' or omit to get all projects.",
                schema = @Schema(allowableValues = {"all", "web", "mobile", "ai", "data"})
            )
            @RequestParam(required = false, defaultValue = "all") String category
    ) {
        log.info("GET /api/projects - category: {}", category);
        
        long startTime = System.currentTimeMillis();
        List<ProjectResponseDto> projects = projectService.getProjectsByCategory(category);
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("GET /api/projects - returned {} projects in {}ms", projects.size(), duration);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_MAX_AGE_SECONDS, TimeUnit.SECONDS).cachePublic())
                .body(projects);
    }

    /**
     * Get a single project by ID.
     *
     * @param id the project ID
     * @return the project
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get project by ID",
        description = "Retrieves a single project by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved project",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid ID format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<ProjectResponseDto> getProjectById(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id
    ) {
        log.info("GET /api/projects/{}", id);
        
        long startTime = System.currentTimeMillis();
        ProjectResponseDto project = projectService.getProjectById(id);
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("GET /api/projects/{} - returned project '{}' in {}ms", id, project.getTitle(), duration);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_MAX_AGE_SECONDS, TimeUnit.SECONDS).cachePublic())
                .body(project);
    }
}
