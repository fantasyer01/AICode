package com.aiblog.repository;

import com.aiblog.model.AestheticsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AestheticsArticleRepository extends JpaRepository<AestheticsArticle, Long> {

    Page<AestheticsArticle> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AestheticsArticle> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<AestheticsArticle> findBySensoryDimensionIdAndPublishedTrueOrderByCreatedAtDesc(Long sensoryDimensionId, Pageable pageable);

    Page<AestheticsArticle> findByDomainDimensionIdAndPublishedTrueOrderByCreatedAtDesc(Long domainDimensionId, Pageable pageable);

    Page<AestheticsArticle> findBySensoryDimensionIdAndDomainDimensionIdAndPublishedTrueOrderByCreatedAtDesc(
            Long sensoryDimensionId, Long domainDimensionId, Pageable pageable);

    @Query("SELECT DISTINCT a FROM AestheticsArticle a JOIN a.tags t WHERE t = :tag AND a.published = true ORDER BY a.createdAt DESC")
    Page<AestheticsArticle> findByTagAndPublishedTrue(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT t FROM AestheticsArticle a JOIN a.tags t ORDER BY t")
    List<String> findAllTags();

    boolean existsBySensoryDimensionId(Long dimensionId);

    boolean existsByDomainDimensionId(Long dimensionId);
}
