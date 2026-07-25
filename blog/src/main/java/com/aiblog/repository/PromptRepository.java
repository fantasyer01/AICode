package com.aiblog.repository;

import com.aiblog.model.Prompt;
import com.aiblog.model.PromptSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long>, JpaSpecificationExecutor<Prompt> {

    Page<Prompt> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Prompt> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Prompt> findBySourceAndPublishedTrueOrderByCreatedAtDesc(PromptSource source, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Prompt a JOIN a.tags t WHERE t = :tag AND a.published = true ORDER BY a.createdAt DESC")
    Page<Prompt> findByTagAndPublishedTrue(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Prompt p JOIN p.tags t ORDER BY t")
    List<String> findAllTags();

    @Query("SELECT DISTINCT p.model FROM Prompt p WHERE p.model IS NOT NULL AND p.model <> '' ORDER BY p.model")
    List<String> findAllModels();
}
