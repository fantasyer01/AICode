package com.portfolio.showcase.service;

import com.portfolio.showcase.dto.ProjectResponseDto;

import java.util.List;

/**
 * Service interface for project-related business operations.
 */
public interface ProjectService {

    /**
     * Retrieves all projects, sorted by date descending.
     *
     * @return list of all projects as DTOs
     */
    List<ProjectResponseDto> getAllProjects();

    /**
     * Retrieves projects filtered by category, sorted by date descending.
     *
     * @param category the category to filter by (web, mobile, ai, data)
     * @return list of filtered projects as DTOs
     * @throws IllegalArgumentException if category is invalid
     */
    List<ProjectResponseDto> getProjectsByCategory(String category);

    /**
     * Retrieves a single project by its ID.
     *
     * @param id the project ID
     * @return the project as a DTO
     * @throws com.portfolio.showcase.exception.ResourceNotFoundException if project not found
     */
    ProjectResponseDto getProjectById(Long id);
}
