package com.aiblog.repository;

import com.aiblog.model.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    Page<Snippet> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
