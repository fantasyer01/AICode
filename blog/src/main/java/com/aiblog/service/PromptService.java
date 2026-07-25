package com.aiblog.service;

import com.aiblog.dto.PromptCreateRequest;
import com.aiblog.dto.PromptResponse;
import com.aiblog.dto.PromptUpdateRequest;
import com.aiblog.model.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromptService {

    PromptResponse create(PromptCreateRequest request);

    PromptResponse getById(Long id);

    PromptResponse update(Long id, PromptUpdateRequest request);

    void delete(Long id);

    Page<PromptResponse> list(Pageable pageable);

    Page<PromptResponse> listPublished(Pageable pageable);

    Page<PromptResponse> search(String keyword, String source, String model, String tag, Pageable pageable);

    Prompt getEntityById(Long id);

    List<String> findAllTags();

    List<String> findAllModels();
}
