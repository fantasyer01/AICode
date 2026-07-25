package com.aiblog.service;

import com.aiblog.dto.AestheticsDimensionRequest;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.model.AestheticsDimension;
import com.aiblog.model.DimensionType;

import java.util.List;

public interface AestheticsDimensionService {

    AestheticsDimensionResponse create(AestheticsDimensionRequest request);

    AestheticsDimensionResponse getById(Long id);

    AestheticsDimensionResponse update(Long id, AestheticsDimensionRequest request);

    void delete(Long id);

    List<AestheticsDimensionResponse> listAll();

    List<AestheticsDimensionResponse> listByType(DimensionType type);

    AestheticsDimension getEntityById(Long id);
}
