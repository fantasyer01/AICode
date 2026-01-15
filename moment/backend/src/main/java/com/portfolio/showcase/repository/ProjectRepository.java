package com.portfolio.showcase.repository;

import com.portfolio.showcase.entity.Project;
import com.portfolio.showcase.entity.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Project entities.
 * Provides CRUD operations and custom query methods for project data access.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find all projects by category, ordered by date descending.
     *
     * @param category the project category to filter by
     * @return list of projects matching the category
     */
    List<Project> findByCategoryOrderByDateDesc(ProjectCategory category);

    /**
     * Find all projects ordered by date descending (newest first).
     *
     * @return list of all projects sorted by date
     */
    List<Project> findAllByOrderByDateDesc();

    /**
     * Find all projects by year, ordered by date descending.
     *
     * @param year the year to filter by
     * @return list of projects from the specified year
     */
    List<Project> findByYearOrderByDateDesc(Integer year);

    /**
     * Check if a project with the given title exists.
     *
     * @param title the project title to check
     * @return true if a project with the title exists, false otherwise
     */
    boolean existsByTitle(String title);
}
