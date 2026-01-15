package com.portfolio.showcase.service;

import com.portfolio.showcase.dto.ProjectResponseDto;
import com.portfolio.showcase.entity.Project;
import com.portfolio.showcase.entity.ProjectCategory;
import com.portfolio.showcase.exception.ResourceNotFoundException;
import com.portfolio.showcase.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ProjectService providing business logic for project operations.
 */
@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<ProjectResponseDto> getAllProjects() {
        log.debug("Fetching all projects");
        
        List<Project> projects = projectRepository.findAllByOrderByDateDesc();
        
        log.info("Retrieved {} projects", projects.size());
        
        return projects.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ProjectResponseDto> getProjectsByCategory(String category) {
        log.debug("Fetching projects with category filter: {}", category);
        
        // Handle 'all' category - return all projects
        if (category == null || category.isBlank() || "all".equalsIgnoreCase(category.trim())) {
            return getAllProjects();
        }

        // Validate and convert category
        if (!ProjectCategory.isValid(category)) {
            log.warn("Invalid category requested: {}", category);
            throw new IllegalArgumentException(
                "Invalid category: " + category + ". Valid values are: all, web, mobile, ai, data"
            );
        }

        ProjectCategory projectCategory = ProjectCategory.fromValue(category);
        List<Project> projects = projectRepository.findByCategoryOrderByDateDesc(projectCategory);
        
        log.info("Retrieved {} projects for category: {}", projects.size(), category);
        
        return projects.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public ProjectResponseDto getProjectById(Long id) {
        log.debug("Fetching project with id: {}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found with id: {}", id);
                    return new ResourceNotFoundException("Project not found with id: " + id);
                });
        
        log.info("Retrieved project: {} (id: {})", project.getTitle(), id);
        
        return mapToDto(project);
    }

    /**
     * Maps a Project entity to ProjectResponseDto.
     *
     * @param project the entity to map
     * @return the mapped DTO
     */
    private ProjectResponseDto mapToDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory().getValue())
                .date(project.getDate())
                .year(project.getYear())
                .tools(project.getTools())
                .color(project.getColor())
                .link(project.getLink())
                .github(project.getGithub())
                .imageUrl(project.getImageUrl())
                .build();
    }
}
