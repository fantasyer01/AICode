package com.aiblog.service;

import com.aiblog.audit.Auditable;
import com.aiblog.dto.AestheticsDimensionRequest;
import com.aiblog.dto.AestheticsDimensionResponse;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.AestheticsDimension;
import com.aiblog.model.DimensionType;
import com.aiblog.repository.AestheticsArticleRepository;
import com.aiblog.repository.AestheticsDimensionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AestheticsDimensionServiceImpl implements AestheticsDimensionService {

    private static final Logger log = LoggerFactory.getLogger(AestheticsDimensionServiceImpl.class);

    private final AestheticsDimensionRepository dimensionRepository;
    private final AestheticsArticleRepository articleRepository;

    public AestheticsDimensionServiceImpl(AestheticsDimensionRepository dimensionRepository,
                                          AestheticsArticleRepository articleRepository) {
        this.dimensionRepository = dimensionRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Auditable(operation = "INSERT", entityType = "AestheticsDimension")
    public AestheticsDimensionResponse create(AestheticsDimensionRequest request) {
        AestheticsDimension dimension = new AestheticsDimension();
        dimension.setName(request.getName());
        dimension.setType(request.getType());
        dimension.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        dimension.setDescription(request.getDescription());

        AestheticsDimension saved = dimensionRepository.save(dimension);
        log.info("Created aesthetics dimension id={}, name='{}', type={}", saved.getId(), saved.getName(), saved.getType());
        return AestheticsDimensionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AestheticsDimensionResponse getById(Long id) {
        return AestheticsDimensionResponse.fromEntity(getEntityById(id));
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "AestheticsDimension")
    public AestheticsDimensionResponse update(Long id, AestheticsDimensionRequest request) {
        AestheticsDimension dimension = getEntityById(id);
        dimension.setName(request.getName());
        dimension.setType(request.getType());
        dimension.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        dimension.setDescription(request.getDescription());

        AestheticsDimension saved = dimensionRepository.save(dimension);
        log.info("Updated aesthetics dimension id={}, name='{}'", saved.getId(), saved.getName());
        return AestheticsDimensionResponse.fromEntity(saved);
    }

    @Override
    @Auditable(operation = "DELETE", entityType = "AestheticsDimension")
    public void delete(Long id) {
        AestheticsDimension dimension = getEntityById(id);

        boolean inUse = articleRepository.existsBySensoryDimensionId(id)
                || articleRepository.existsByDomainDimensionId(id);
        if (inUse) {
            throw new IllegalStateException("Cannot delete dimension '" + dimension.getName()
                    + "' because it is referenced by existing aesthetics articles");
        }

        dimensionRepository.delete(dimension);
        log.info("Deleted aesthetics dimension id={}, name='{}'", id, dimension.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AestheticsDimensionResponse> listAll() {
        return dimensionRepository.findAllByOrderByTypeAscDisplayOrderAscNameAsc()
                .stream()
                .map(AestheticsDimensionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AestheticsDimensionResponse> listByType(DimensionType type) {
        return dimensionRepository.findByTypeOrderByDisplayOrderAscNameAsc(type)
                .stream()
                .map(AestheticsDimensionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AestheticsDimension getEntityById(Long id) {
        return dimensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AestheticsDimension", id));
    }
}
