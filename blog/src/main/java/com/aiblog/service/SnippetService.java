package com.aiblog.service;

import com.aiblog.dto.SnippetCreateRequest;
import com.aiblog.dto.SnippetResponse;
import com.aiblog.model.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;

public interface SnippetService {

    SnippetResponse create(SnippetCreateRequest request);

    SnippetResponse getById(Long id);

    SnippetResponse update(Long id, SnippetCreateRequest request);

    void delete(Long id);

    Page<SnippetResponse> list(Pageable pageable);

    Snippet getEntityById(Long id);

    LinkedHashMap<String, List<SnippetResponse>> listGroupedByDate(Pageable pageable);
}
