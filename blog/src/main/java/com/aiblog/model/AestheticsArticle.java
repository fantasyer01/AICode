package com.aiblog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aesthetics_articles")
@Getter
@Setter
@NoArgsConstructor
public class AestheticsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 200)
    private String author;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String content;

    @Column(length = 1000)
    private String summary;

    @Column(length = 2000)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensory_dimension_id", nullable = false)
    private AestheticsDimension sensoryDimension;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_dimension_id", nullable = false)
    private AestheticsDimension domainDimension;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aesthetics_article_tags", joinColumns = @JoinColumn(name = "aesthetics_article_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = true)
    private Boolean published = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
