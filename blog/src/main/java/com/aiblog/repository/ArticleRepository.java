package com.aiblog.repository;

import com.aiblog.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Article a JOIN a.tags t WHERE t = :tag ORDER BY a.createdAt DESC")
    Page<Article> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Article a JOIN a.tags t ORDER BY t")
    List<String> findAllTags();

    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.category IS NOT NULL ORDER BY a.category")
    List<String> findAllCategories();

    Page<Article> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    Page<Article> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Article a JOIN a.tags t WHERE t = :tag AND a.published = true ORDER BY a.createdAt DESC")
    Page<Article> findByTagAndPublishedTrue(@Param("tag") String tag, Pageable pageable);

    Page<Article> findByCategoryAndPublishedTrueOrderByCreatedAtDesc(String category, Pageable pageable);
}
