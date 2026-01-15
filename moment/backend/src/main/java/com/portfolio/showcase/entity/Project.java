package com.portfolio.showcase.entity;

import com.portfolio.showcase.converter.StringListConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity representing a project in the portfolio showcase.
 * Maps to the 'projects' table in the database.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_category", columnList = "category"),
    @Index(name = "idx_project_year", columnList = "year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 2000, message = "Description must be between 1 and 2000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectCategory category;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Date must be in YYYY-MM format")
    @Column(nullable = false, length = 7)
    private String date;

    @NotNull(message = "Year is required")
    @Column(nullable = false)
    private Integer year;

    @NotNull(message = "Tools array is required")
    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    @Builder.Default
    private List<String> tools = new ArrayList<>();

    @NotBlank(message = "Color is required")
    @Size(max = 10, message = "Color must be at most 10 characters")
    @Column(nullable = false, length = 10)
    private String color;

    @Size(max = 500, message = "Link must be at most 500 characters")
    @Column(length = 500)
    private String link;

    @Size(max = 500, message = "GitHub URL must be at most 500 characters")
    @Column(length = 500)
    private String github;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", date='" + date + '\'' +
                ", year=" + year +
                '}';
    }
}
