package com.ithistory.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "stories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date_month", "date_day"})
})
public class Story {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "date_month", nullable = false)
    private Integer dateMonth;
    
    @Column(name = "date_day", nullable = false)
    private Integer dateDay;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "epilogue", columnDefinition = "TEXT")
    private String epilogue;
    
    @Column(name = "references", columnDefinition = "TEXT")
    private String references;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StoryStatus status = StoryStatus.PUBLISHED;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum StoryStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
